package com.servicedesk.app.data.api

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TicketsApi {
    @GET("api/tickets/columns")
    suspend fun columns(): TicketColumnsResponseDto

    @GET("api/tickets/issue-types")
    suspend fun issueTypes(@Query("lang") lang: String = "ru"): IssueTypesResponseDto

    @GET("api/tickets")
    suspend fun list(): TicketListResponseDto

    @GET("api/tickets/filter")
    suspend fun filter(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("tag_str") tagStr: String? = null,
        @Query("serial_number") serialNumber: String? = null,
        @Query("manager") manager: Int? = null,
    ): TicketListResponseDto

    @GET("api/tickets/{id}")
    suspend fun get(@Path("id") id: Int): TicketDetailDto

    @POST("api/tickets")
    suspend fun create(@Body body: TicketCreateRequestDto): TicketDetailDto

    @PATCH("api/tickets/{id}")
    suspend fun patch(@Path("id") id: Int, @Body body: TicketPatchRequestDto): TicketDetailDto
}

@Serializable
data class TicketColumnDto(
    val id: String,
    val status: String,
    val color: String,
    val isClosed: Boolean = false,
)

@Serializable
data class TicketColumnsResponseDto(
    val defaultStatus: String,
    val columns: List<TicketColumnDto>,
)

@Serializable
data class IssueTypeHierarchyItemDto(
    val issueType: String,
    val subIssueTypes: List<String> = emptyList(),
)

@Serializable
data class IssueTypesResponseDto(
    val lang: String,
    val hierarchy: List<IssueTypeHierarchyItemDto>,
    val thirdIssueTypes: List<String> = emptyList(),
    val labels: Map<String, String> = emptyMap(),
)

@Serializable
data class TicketShortDto(
    val ticketId: Int,
    val userId: Int,
    val tags: List<String>? = null,
    val createdAt: String,
    val clientName: String? = null,
    val clientAvatarUrl: String? = null,
    val lastMessage: String? = null,
    val status: String,
    val assignedTo: Int? = null,
    val assigneeName: String? = null,
    val unreadCount: Int = 0,
    val serialNumber: String? = null,
)

@Serializable
data class TicketListResponseDto(
    val tickets: List<TicketShortDto>,
)

@Serializable
data class TicketCreateRequestDto(
    val userId: Int,
    val reason: String? = null,
)

@Serializable
data class ReplacementPartDto(
    val sku: String? = null,
    val qty: Int? = null,
    val remark: String? = null,
)

@Serializable
data class TicketPatchRequestDto(
    val status: String? = null,
    val reason: String? = null,
    val printerModel: String? = null,
    val serialNumber: String? = null,
    val trackingNumber: String? = null,
    val deliveryAddress: String? = null,
    val warrantyCase: Boolean? = null,
    val purchasedFrom: String? = null,
    val sku: String? = null,
    val deliveryCost: Double? = null,
    val solutionDescription: String? = null,
    val fullName: String? = null,
    val contactPhone: String? = null,
    val externalExportUrl: String? = null,
    val serviceType: String? = null,
    val serviceTime: String? = null,
    val replenishmentRequirement: String? = null,
    val serviceChannel: List<String>? = null,
    val serviceSolution: String? = null,
    val warrantyStatus: String? = null,
    val issueType: String? = null,
    val subIssueType: String? = null,
    val thirdIssueType: String? = null,
    val qtyUsed: Int? = null,
    val replacementQty: Int? = null,
    val replenishmentQty: Int? = null,
    val skuRemark: String? = null,
    val extraSerialNumber: String? = null,
    val purchaseOrderId: String? = null,
    val productType: String? = null,
    val replacementParts: List<ReplacementPartDto>? = null,
)

@Serializable
data class TicketDetailDto(
    val id: Int,
    val userId: Int,
    val status: String,
    val tags: List<String>? = null,
    val createdAt: String,
    val updatedAt: String? = null,
    val reason: String? = null,
    val printerModel: String? = null,
    val serialNumber: String? = null,
    val trackingNumber: String? = null,
    val deliveryAddress: String? = null,
    val warrantyCase: Boolean? = null,
    val purchasedFrom: String? = null,
    val sku: String? = null,
    val deliveryCost: Double? = null,
    val solutionDescription: String? = null,
    val fullName: String? = null,
    val contactPhone: String? = null,
    val externalExportUrl: String? = null,
    val assignedTo: Int? = null,
    val maxUserId: Long? = null,
    val serviceType: String? = null,
    val serviceTime: String? = null,
    val replenishmentRequirement: String? = null,
    val serviceChannel: List<String>? = null,
    val serviceSolution: String? = null,
    val warrantyStatus: String? = null,
    val issueType: String? = null,
    val subIssueType: String? = null,
    val thirdIssueType: String? = null,
    val qtyUsed: Int? = null,
    val replacementQty: Int? = null,
    val replenishmentQty: Int? = null,
    val skuRemark: String? = null,
    val extraSerialNumber: String? = null,
    val purchaseOrderId: String? = null,
    val productType: String? = null,
    val replacementParts: List<ReplacementPartDto>? = null,
)
