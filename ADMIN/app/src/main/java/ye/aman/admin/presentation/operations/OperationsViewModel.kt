package ye.aman.admin.presentation.operations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ye.aman.admin.core.result.AppResult
import ye.aman.admin.domain.model.*
import ye.aman.admin.domain.usecase.OperationsUseCases

data class OperationsUiState(
    val selectedTab: Int = 0, // 0: Requests, 1: Tasks, 2: Protections
    val requests: List<AdminProtectionRequest> = emptyList(),
    val tasks: List<PaymentTask> = emptyList(),
    val protections: List<AdminProtection> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedRequest: AdminProtectionRequest? = null,
    val selectedTask: PaymentTask? = null,
    val isApproveDialogOpen: Boolean = false,
    val isRejectDialogOpen: Boolean = false,
    val isExecuteTaskDialogOpen: Boolean = false,
    val isRescheduleDialogOpen: Boolean = false
)

class OperationsViewModel(
    private val useCases: OperationsUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(OperationsUiState())
    val uiState: StateFlow<OperationsUiState> = _uiState.asStateFlow()

    init {
        observeData()
        refreshAll()
    }

    private fun observeData() {
        viewModelScope.launch {
            useCases.getProtectionRequests().collect { list ->
                _uiState.value = _uiState.value.copy(requests = list)
            }
        }
        viewModelScope.launch {
            useCases.getTasks().collect { list ->
                _uiState.value = _uiState.value.copy(tasks = list)
            }
        }
        viewModelScope.launch {
            useCases.getProtections().collect { list ->
                _uiState.value = _uiState.value.copy(protections = list)
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun openApproveDialog(req: AdminProtectionRequest) {
        _uiState.value = _uiState.value.copy(selectedRequest = req, isApproveDialogOpen = true)
    }

    fun openRejectDialog(req: AdminProtectionRequest) {
        _uiState.value = _uiState.value.copy(selectedRequest = req, isRejectDialogOpen = true)
    }

    fun openExecuteTaskDialog(task: PaymentTask) {
        _uiState.value = _uiState.value.copy(selectedTask = task, isExecuteTaskDialogOpen = true)
    }

    fun openRescheduleTaskDialog(task: PaymentTask) {
        _uiState.value = _uiState.value.copy(selectedTask = task, isRescheduleDialogOpen = true)
    }

    fun dismissDialogs() {
        _uiState.value = _uiState.value.copy(
            isApproveDialogOpen = false,
            isRejectDialogOpen = false,
            isExecuteTaskDialogOpen = false,
            isRescheduleDialogOpen = false,
            selectedRequest = null,
            selectedTask = null
        )
    }

    fun approveRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val res = useCases.approveRequest(requestId)) {
                is AppResult.Success -> {
                    dismissDialogs()
                    refreshAll()
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = res.error.message)
                }
                is AppResult.Loading -> Unit
            }
        }
    }

    fun rejectRequest(requestId: String, reason: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val res = useCases.rejectRequest(requestId, reason)) {
                is AppResult.Success -> {
                    dismissDialogs()
                    refreshAll()
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = res.error.message)
                }
                is AppResult.Loading -> Unit
            }
        }
    }

    fun executeTask(taskId: String, notes: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val res = useCases.executeTask(taskId, notes)) {
                is AppResult.Success -> {
                    dismissDialogs()
                    refreshAll()
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = res.error.message)
                }
                is AppResult.Loading -> Unit
            }
        }
    }

    fun rescheduleTask(taskId: String, newDueAt: String, reason: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val res = useCases.rescheduleTask(taskId, newDueAt, reason)) {
                is AppResult.Success -> {
                    dismissDialogs()
                    refreshAll()
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = res.error.message)
                }
                is AppResult.Loading -> Unit
            }
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            useCases.refreshRequests()
            useCases.refreshTasks()
            useCases.refreshProtections()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}
