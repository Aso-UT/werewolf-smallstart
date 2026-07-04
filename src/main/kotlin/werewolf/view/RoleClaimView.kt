package werewolf.view

data class RoleClaimEntry(val claimant: String, val role: String)

data class RoleClaimView(val roleClaims: List<RoleClaimEntry>)
