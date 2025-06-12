document.addEventListener('DOMContentLoaded', () => {
    // --- DOM Element References ---
    const planListContainer = document.getElementById('plan-list-container');
    const modalOverlay = document.getElementById('modal-overlay');
    const showRegisterModalBtn = document.getElementById('show-register-modal-btn');

    // Tab and Filter Elements
    const planTabs = document.querySelector('.plan-tabs');
    const toggleFilterBtn = document.getElementById('toggle-filter-btn');
    const filterSection = document.getElementById('filter-section');
    const filterForm = document.getElementById('filter-form');

    // Plan Modal Elements
    const planModal = document.getElementById('plan-modal');
    const planModalTitle = document.getElementById('plan-modal-title');
    const planForm = document.getElementById('plan-form');
    const benefitManagementSection = document.getElementById('benefit-management-section');
    const submitPlanBtn = document.getElementById('submit-plan-btn');
    const cancelPlanBtn = document.getElementById('cancel-plan-btn');

    // Benefit Modal Elements
    const benefitModal = document.getElementById('benefit-modal');
    const benefitModalTitle = document.getElementById('benefit-modal-title');
    const benefitList = document.getElementById('benefit-list');
    const applyBenefitChangesBtn = document.getElementById('apply-benefit-changes-btn');
    const cancelBenefitBtn = document.getElementById('cancel-benefit-btn');

    // 새로 추가: 총 요금제 개수 표시를 위한 DOM 요소 참조
    const totalPlanCountSpan = document.getElementById('total-plan-count');

    // --- State Management ---
    let currentPlanId = null;
    let stagedBenefitIds = new Set();
    // [수정] 카테고리 이름을 ID로 관리
    let activeCategoryId = null; // null은 '전체'를 의미
    const API_BASE_URL = '/plan';

    // --- API Fetch Functions ---
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
        filterPlans: (filterData) => fetch(`${API_BASE_URL}/filter`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(filterData),
        }).then(res => res.json()),
    };

    // --- Modal Control ---
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
            planListContainer.innerHTML = '<p>조건에 맞는 요금제가 없습니다.</p>';
            totalPlanCountSpan.textContent = 0; // 요금제가 없으면 0개로 표시
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
        totalPlanCountSpan.textContent = plans.length; // 요금제 개수 업데이트
    };

    // --- Core Loading Function ---
    const loadPlans = async (filterData = {}) => {
        const requestData = { ...filterData };

        // [수정] 활성화된 탭의 카테고리 ID를 필터 조건에 추가
        if (activeCategoryId) {
            requestData.categoryIds = [activeCategoryId];
        }

        try {
            // 필터링 조건이 하나라도 있는지 확인 (카테고리 탭 선택 포함)
            const isFiltering = activeCategoryId || Object.values(requestData).some(v => (Array.isArray(v) && v.length > 0) || v === true);

            let response;
            if (isFiltering) {
                // 필터링 요청 시
                response = await api.filterPlans(requestData);
            } else {
                // 전체 목록 요청 시
                response = await api.getPlans();
            }

            // API 응답 구조에 따라 'data' 속성 또는 직접 응답 사용
            const plans = response.data || response;
            renderPlanList(plans); // 요금제 목록을 렌더링하고 개수를 업데이트

        } catch (error) {
            console.error('요금제 목록 로딩 실패:', error);
            planListContainer.innerHTML = `<p>요금제 목록을 불러오는 중 오류가 발생했습니다.</p>`;
            totalPlanCountSpan.textContent = 0; // 오류 발생 시 0개로 표시
        }
    };

    // --- Event Handlers ---
    const handleTabClick = (e) => {
        const selectedTab = e.target.closest('.tab-btn');
        if (!selectedTab) return;

        planTabs.querySelector('.active').classList.remove('active');
        selectedTab.classList.add('active');

        // [수정] data-category-id 값을 읽어 숫자로 변환. 없으면 null ('전체' 탭)
        const categoryId = selectedTab.dataset.categoryId;
        activeCategoryId = categoryId ? parseInt(categoryId, 10) : null;

        filterForm.reset();
        handleFilterCheckboxes();
        loadPlans(); // 탭 변경 시 요금제 다시 로드
    };

    const handleFilterToggle = () => {
        filterSection.classList.toggle('hidden');
        toggleFilterBtn.textContent = filterSection.classList.contains('hidden')
            ? '요금제 필터 열기'
            : '요금제 필터 닫기';
    };

    const handleFilterSubmit = (e) => {
        e.preventDefault();
        const priceRanges = Array.from(filterForm.querySelectorAll('input[name="price"]:checked')).map(el => el.value);
        const dataOptions = Array.from(filterForm.querySelectorAll('input[name="data"]:checked')).map(el => el.value);
        const benefitIds = Array.from(filterForm.querySelectorAll('input[name="benefit"]:checked')).map(el => parseInt(el.value));

        const filterData = {
            anyPriceSelected: filterForm.querySelector('#anyPrice').checked,
            priceRanges: filterForm.querySelector('#anyPrice').checked ? [] : priceRanges,

            anyDataSelected: filterForm.querySelector('#anyData').checked,
            dataOptions: filterForm.querySelector('#anyData').checked ? [] : dataOptions,

            noBenefitsSelected: filterForm.querySelector('#noBenefits').checked,
            benefitIds: filterForm.querySelector('#noBenefits').checked ? [] : benefitIds,
        };

        loadPlans(filterData); // 필터 적용 시 요금제 로드
    };

    const handleFilterCheckboxes = () => {
        document.querySelectorAll('input[name="price"]').forEach(cb => cb.disabled = document.getElementById('anyPrice').checked);
        document.querySelectorAll('input[name="data"]').forEach(cb => cb.disabled = document.getElementById('anyData').checked);
        document.querySelectorAll('input[name="benefit"]').forEach(cb => cb.disabled = document.getElementById('noBenefits').checked);
    };

    const handleRegisterPlan = () => {
        currentPlanId = null;
        planForm.reset();
        planModalTitle.textContent = '요금제 등록';
        submitPlanBtn.textContent = '등록하기';
        benefitManagementSection.classList.remove('hidden');
        document.getElementById('planId').value = '';
        document.getElementById('benefitIdList').value = '';
        stagedBenefitIds.clear();
        openModal(planModal);
    };

    const handleEditPlan = async (planId) => {
        try {
            const response = await api.getPlanById(planId);
            if (response.statusCode !== 200) throw new Error(response.data.message);

            const plan = response.data;
            currentPlanId = plan.planId;
            planForm.reset();

            for (const key in plan) {
                if (planForm.elements[key]) {
                    planForm.elements[key].value = Array.isArray(plan[key]) ? plan[key].join(',') : plan[key];
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

            if (fetchResponse.ok && (result.statusCode === 200 || result.statusCode === 201)) {
                alert(`요금제가 성공적으로 ${currentPlanId ? '수정' : '등록'}되었습니다.`);
                closeAllModals();
                loadPlans(); // 변경 후 요금제 다시 로드
            } else {
                throw new Error(result.data.message || '알 수 없는 오류가 발생했습니다.');
            }
        } catch (error) {
            alert(`오류 발생: ${error.message}`);
        }
    };

    // Benefit-related handlers (Unchanged)
    const handleOpenBenefitModal = async (event) => {
        const benefitType = event.target.dataset.benefitType;
        const currentBenefitIds = document.getElementById('benefitIdList').value.split(',').filter(Boolean).map(Number);
        stagedBenefitIds = new Set(currentBenefitIds);

        try {
            const response = await api.getBenefitsByType(benefitType.toUpperCase());
            if (response.statusCode !== 200) throw new Error(response.data.message);

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
        benefitModal.classList.add('hidden');
    };

    // --- Event Listeners ---
    showRegisterModalBtn.addEventListener('click', handleRegisterPlan);
    cancelPlanBtn.addEventListener('click', closeAllModals);
    modalOverlay.addEventListener('click', closeAllModals);
    planForm.addEventListener('submit', handlePlanFormSubmit);

    planTabs.addEventListener('click', handleTabClick);
    toggleFilterBtn.addEventListener('click', handleFilterToggle);
    filterForm.addEventListener('submit', handleFilterSubmit);
    filterForm.addEventListener('reset', () => {
        setTimeout(() => {
            handleFilterCheckboxes();
            loadPlans();
        }, 0);
    });
    filterForm.addEventListener('change', (e) => {
        if (e.target.type === 'checkbox') {
            handleFilterCheckboxes();
        }
    });

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