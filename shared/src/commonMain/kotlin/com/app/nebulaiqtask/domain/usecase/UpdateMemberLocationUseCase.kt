package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.model.LocationCoordinate
import com.app.nebulaiqtask.domain.repository.MemberRepository

class UpdateMemberLocationUseCase(
    private val memberRepository: MemberRepository
) {
    suspend operator fun invoke(
        memberId: String,
        location: LocationCoordinate,
        isInside: Boolean,
        distanceToFence: Double
    ) {
        memberRepository.updateMemberLocation(memberId, location, isInside, distanceToFence)
    }
}
