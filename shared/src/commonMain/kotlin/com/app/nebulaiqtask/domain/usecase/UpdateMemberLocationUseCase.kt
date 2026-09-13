package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.model.LocationCoordinate
import com.app.nebulaiqtask.domain.repository.MemberRepository

class UpdateMemberLocationUseCase(
    private val memberRepository: MemberRepository
) {
    suspend operator fun invoke(
        groupId: String,
        memberId: String,
        location: LocationCoordinate,
        battery: Int = 100,
        isInside: Boolean,
        distanceToFence: Double
    ) {
        memberRepository.updateMemberLocation(groupId, memberId, location, battery, isInside, distanceToFence)
    }
}
