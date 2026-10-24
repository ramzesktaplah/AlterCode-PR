package com.example.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.SyntaxPalette

object SyntaxHighlighter {
    private val keywordPattern = Regex(
        "\\b(package|import|class|interface|object|fun|val|var|private|public|protected|internal|" +
            "suspend|override|return|if|else|when|for|while|do|break|continue|try|catch|finally|throw|" +
            "true|false|null|def|from|async|await|with|as|lambda|yield|pass|None|True|False|self|" +
            "export|const|let|function|type|readonly|implements|extends|new|typeof|instanceof|" +
            "fn|pub|struct|impl|mut|use|mod|match|enum|trait|where|move|ref|crate|super|" +
            "func|defer|go|chan|select|map|range|fallthrough|include|namespace|using|template|" +
            "typename|virtual|constexpr|static_cast|actor|guard|protocol|extension|SELECT|FROM|" +
            "WHERE|GROUP|BY|ORDER|INSERT|INTO|VALUES|UPDATE|DELETE|JOIN|LEFT|INNER|LIMIT|AS|AND|OR|COUNT|ROUND|AVG)\\b"
    )

    private val typePattern = Regex(
        "\\b(String|Int|Long|Double|Float|Boolean|Unit|Any|Nothing|List|Map|Set|ArrayDeque|Flow|" +
            "StateFlow|Result|Mutex|VecDeque|Arc|Option|usize|u64|i64|i32|u32|str|bool|Promise|" +
            "Record|Array|ReadonlyArray|Request|Response|DurableObjectNamespace|DurableObjectState|" +
            "vector|string|deque|dict|tuple|Optional|IEnumerable)\\b"
    )

    private val numberPattern = Regex("\\b\\d+(?:_[0-9]+)*(?:\\.[0-9]+)?[Lufd]?\\b")
    private val functionCallPattern = Regex("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?=\\()")
    private val stringPattern = Regex("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'")
    private val commentPattern = Regex("//.*$|#.*$|/\\*[\\s\\S]*?\\*/", RegexOption.MULTILINE)

    fun highlight(code: String, palette: SyntaxPalette): AnnotatedString {
        if (code.isEmpty()) return AnnotatedString("")

        return buildAnnotatedString {
            append(code)
            addStyle(SpanStyle(color = palette.plain), 0, code.length)

            // Numbers
            numberPattern.findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.number),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // Function calls
            functionCallPattern.findAll(code).forEach { match ->
                val group = match.groups[1] ?: return@forEach
                addStyle(
                    SpanStyle(color = palette.function),
                    group.range.first,
                    group.range.last + 1
                )
            }

            // Types
            typePattern.findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.type, fontWeight = FontWeight.SemiBold),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // Keywords
            keywordPattern.findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.keyword, fontWeight = FontWeight.Bold),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // Strings
            stringPattern.findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.string),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // Comments
            commentPattern.findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.comment, fontStyle = FontStyle.Italic),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }
    }
}
