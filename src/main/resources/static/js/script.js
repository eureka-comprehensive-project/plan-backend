document.addEventListener('DOMContentLoaded', () => {
    // --- DOM Element References ---
    const planListContainer = document.getElementById('plan-list-container');
    const modalOverlay = document.getElementById('modal-overlay');

    // Plan Modal Elements
    const planModal = document.getElementById('plan-modal');
    const planModalTitle = document.getElementById('plan-modal-title');
    const planForm = document.getElementById('plan-form');
    const benefitManagementSection = document.getElementById('benefit-management-section');
    const submitPlanBtn = document.getElementById('submit-plan-btn');
    const cancelPlanBtn = document.getElementById('cancel-plan-btn');
    const showRegisterModalBtn = document.getElementById('show-register-modal-btn');

    // Benefit Modal Elements
    const benefitModal = document.getElementById('benefit-modal');
    const benefitModalTitle = document.getElementById('benefit-modal-title');
    const benefitList = document.getElementById('benefit-list');
    const applyBenefitChangesBtn = document.getElementById('apply-benefit-changes-btn');
    const cancelBenefitBtn = document.getElementById('cancel-benefit-btn');

    // --- State Management ---
    let currentPlanId = null;
    let stagedBenefitIds = new Set();
    const API_BASE_URL = '/plan';

    // --- API Fetch Functions (수정 없음) ---
    const api = {
        getPlans: () => fetch(API_BASE_URL).then(res => res.json()),
        getPlanById: (id) => fetch(`${API_BASE_URL}/${id}`).then(res => res.json()),
        createPlan: (data) => fetch(API_BASE_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        }),
        updatePlan: (id, data) => fetch(`${API_BASE_URL}/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        }),
        getBenefitsByType: (type) => fetch(`${API_BASE_URL}/benefit/${type}`).then(res => res.json()),
    };

    // --- Modal Control (수정 없음) ---
    const openModal = (modalElement) => {
        modalOverlay.classList.remove('hidden');
        modalElement.classList.remove('hidden');
    };

    const closeAllModals = () => {
        modalOverlay.classList.add('hidden');
        planModal.classList.add('hidden');
        benefitModal.classList.add('hidden');
    };

    // --- Render Functions ---
    const renderPlanList = (plans) => {
        planListContainer.innerHTML = '';
        if (!plans || plans.length === 0) {
            planListContainer.innerHTML = '<p>등록된 요금제가 없습니다.</p>';
            return;
        }
        plans.forEach(plan => {
            const planItem = document.createElement('div');
            planItem.className = 'plan-item';
            planItem.dataset.planId = plan.planId;
            planItem.innerHTML = `
                <h3>${plan.planName}</h3>
                <p><strong>카테고리:</strong> ${plan.planCategory}</p>
                <p><strong>월정액:</strong> ${plan.monthlyFee.toLocaleString()}원</p>
            `;
            planItem.addEventListener('click', () => handleEditPlan(plan.planId));
            planListContainer.appendChild(planItem);
        });
    };

    const loadPlans = async () => {
        try {
            const response = await api.getPlans();
            if (response.statusCode === 200) {
                renderPlanList(response.data);
            } else {
                throw new Error(response.data.message);
            }
        } catch (error) {
            console.error('요금제 목록 로딩 실패:', error);
            planListContainer.innerHTML = `<p>요금제 목록을 불러오는 중 오류가 발생했습니다.</p>`;
        }
    };

    // --- Event Handlers ---
    const handleRegisterPlan = () => {
        currentPlanId = null;
        planForm.reset();
        planModalTitle.textContent = '요금제 등록';
        submitPlanBtn.textContent = '등록하기';
        // [수정] 요금제 등록 시에도 혜택 관리 섹션을 보여주도록 변경
        benefitManagementSection.classList.remove('hidden');
        document.getElementById('planId').value = '';
        document.getElementById('benefitIdList').value = '';
        stagedBenefitIds.clear(); // 기존에 선택된 혜택 ID 초기화
        openModal(planModal);
    };

    const handleEditPlan = async (planId) => {
        try {
            const response = await api.getPlanById(planId);
            if (response.statusCode !== 200) {
                throw new Error(response.data.message);
            }

            const plan = response.data;
            currentPlanId = plan.planId;
            planForm.reset();

            for (const key in plan) {
                const field = planForm.elements[key];
                if (field) {
                    if (key === 'benefitIdList') {
                        field.value = plan[key].join(',');
                    } else {
                        field.value = plan[key];
                    }
                }
            }
            planModalTitle.textContent = '요금제 수정';
            submitPlanBtn.textContent = '수정 완료';
            benefitManagementSection.classList.remove('hidden');
            openModal(planModal);
        } catch (error) {
            alert('요금제 정보를 불러오지 못했습니다: ' + error.message);
        }
    };

    const handlePlanFormSubmit = async (event) => {
        event.preventDefault();
        const formData = new FormData(planForm);
        const planData = {};
        formData.forEach((value, key) => {
            if (['monthlyFee', 'dataAllowance', 'tetheringDataAmount', 'familyDataAmount', 'voiceAllowance', 'additionalCallAllowance'].includes(key)) {
                planData[key] = parseInt(value, 10) || 0;
            } else {
                planData[key] = value;
            }
        });

        planData.benefitIdList = formData.get('benefitIdList').split(',').filter(Boolean).map(Number);

        try {
            const fetchResponse = currentPlanId
                ? await api.updatePlan(currentPlanId, planData)
                : await api.createPlan(planData);

            const result = await fetchResponse.json();

            if (fetchResponse.ok && (result.statusCode === 200 || result.statusCode === 201)) { // 201 Created도 성공으로 간주
                alert(`요금제가 성공적으로 ${currentPlanId ? '수정' : '등록'}되었습니다.`);
                closeAllModals();
                loadPlans();
            } else {
                throw new Error(result.data.message || '알 수 없는 오류가 발생했습니다.');
            }
        } catch (error) {
            alert(`오류 발생: ${error.message}`);
        }
    };

    const handleOpenBenefitModal = async (event) => {
        const benefitType = event.target.dataset.benefitType;
        const currentBenefitIds = document.getElementById('benefitIdList').value.split(',').filter(Boolean).map(Number);
        stagedBenefitIds = new Set(currentBenefitIds);

        try {
            const response = await api.getBenefitsByType(benefitType.toUpperCase());
            if (response.statusCode !== 200) {
                throw new Error(response.data.message);
            }

            benefitModalTitle.textContent = `${benefitType === 'PREMIUM' ? '프리미엄' : '미디어'} 혜택 관리`;
            benefitList.innerHTML = '';

            response.data.forEach(benefit => {
                const hasBenefit = stagedBenefitIds.has(benefit.benefitId);
                const item = document.createElement('div');
                item.className = 'benefit-item';
                item.innerHTML = `
                    <span class="benefit-item-name">${benefit.benefitName}</span>
                    <button type="button" class="benefit-action-btn ${hasBenefit ? 'btn-remove' : 'btn-add'}" data-benefit-id="${benefit.benefitId}">
                        ${hasBenefit ? '-' : '+'}
                    </button>
                `;
                benefitList.appendChild(item);
            });
            openModal(benefitModal);
        } catch (error) {
            alert('혜택 정보를 불러오는 데 실패했습니다: ' + error.message);
        }
    };

    const handleBenefitAction = (event) => {
        if (!event.target.matches('.benefit-action-btn')) return;

        const button = event.target;
        const benefitId = Number(button.dataset.benefitId);

        if (stagedBenefitIds.has(benefitId)) {
            stagedBenefitIds.delete(benefitId);
            button.classList.replace('btn-remove', 'btn-add');
            button.textContent = '+';
        } else {
            stagedBenefitIds.add(benefitId);
            button.classList.replace('btn-add', 'btn-remove');
            button.textContent = '-';
        }
    };

    const handleApplyBenefitChanges = () => {
        document.getElementById('benefitIdList').value = Array.from(stagedBenefitIds).join(',');
        benefitModal.classList.add('hidden'); // Close only benefit modal
    };


    // --- Event Listeners (수정 없음) ---
    showRegisterModalBtn.addEventListener('click', handleRegisterPlan);
    cancelPlanBtn.addEventListener('click', closeAllModals);
    modalOverlay.addEventListener('click', closeAllModals);
    planForm.addEventListener('submit', handlePlanFormSubmit);

    benefitManagementSection.addEventListener('click', (e) => {
        if (e.target.matches('[data-benefit-type]')) {
            handleOpenBenefitModal(e);
        }
    });
    benefitList.addEventListener('click', handleBenefitAction);
    applyBenefitChangesBtn.addEventListener('click', handleApplyBenefitChanges);
    cancelBenefitBtn.addEventListener('click', () => benefitModal.classList.add('hidden'));

    // --- Initial Load ---
    loadPlans();
});