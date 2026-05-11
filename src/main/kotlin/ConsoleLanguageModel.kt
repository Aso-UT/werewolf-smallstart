package org.example

class ConsoleLanguageModel : LanguageModel {
    override fun ask(prompt: String): String {
        println(prompt)
        print("回答 > ")
        return readLine()?.trim() ?: ""
    }
}
