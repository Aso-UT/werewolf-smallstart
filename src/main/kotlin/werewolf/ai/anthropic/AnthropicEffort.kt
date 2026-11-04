package werewolf.ai.anthropic

import com.anthropic.models.messages.OutputConfig

enum class AnthropicEffort(private val sdkEffort: OutputConfig.Effort) {
    LOW(OutputConfig.Effort.LOW),
    MEDIUM(OutputConfig.Effort.MEDIUM),
    HIGH(OutputConfig.Effort.HIGH),
    XHIGH(OutputConfig.Effort.XHIGH),
    MAX(OutputConfig.Effort.MAX),
    ;

    fun toSdkEffort(): OutputConfig.Effort = sdkEffort
}
