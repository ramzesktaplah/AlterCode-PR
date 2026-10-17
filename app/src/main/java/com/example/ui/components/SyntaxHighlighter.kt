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
    private val annotationPattern = Regex("@[A-Za-z_][A-Za-z0-9_]*|#\\[[A-Za-z_][^\\]]*\\]")
    private val functionCallPattern = Regex("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?=\\()")
    private val stringPattern = Regex("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'")
    private val commentPattern = Regex("//.*$|#.*$|/\\*[\\s\\S]*?\\*/", RegexOption.MULTILINE)

    fun highlight(code: String, palette: SyntaxPalette): AnnotatedString {
        if (code.isEmpty()) return AnnotatedString("")

        return buildAnnotatedString {
            append(code)
            addStyle(SpanStyle(color = palette.plainText), 0, code.length)

            // 1. Numbers
            numberPattern.findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.numberLiteral),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // 2. Function calls
            functionCallPattern.findAll(code).forEach { match ->
                val group = match.groups[1] ?: return@forEach
                addStyle(
                    SpanStyle(color = palette.functionName),
                    group.range.first,
                    group.range.last + 1
                )
            }

            // 3. Types
            typePattern.findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.typeName, fontWeight = FontWeight.Medium),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // 4. Keywords
            keywordPattern.findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.keyword, fontWeight = FontWeight.SemiBold),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // 5. Annotations / Attributes
            annotationPattern.findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.annotation, fontWeight = FontWeight.Medium),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // 6. Strings
            stringPattern.findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.stringLiteral),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // 7. Comments override everything inside their span
            commentPattern.findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.comment, fontStyle = FontStyle.Italic),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }
    }

    enum class DiffKind { UNCHANGED, ADDED, REMOVED }

    data class DiffLine(
        val kind: DiffKind,
        val oldLineNum: Int?,
        val newLineNum: Int?,
        val content: String
    )

    fun computeLineDiff(before: String, after: String): List<DiffLine> {
        val oldLines = before.lines()
        val newLines = after.lines()
        val result = mutableListOf<DiffLine>()

        var i = 0
        var j = 0
        while (i < oldLines.size || j < newLines.size) {
            val oldLine = oldLines.getOrNull(i)
            val newLine = newLines.getOrNull(j)
            when {
                oldLine != null && newLine != null && oldLine.trim() == newLine.trim() -> {
                    result.add(DiffLine(DiffKind.UNCHANGED, i + 1, j + 1, newLine))
                    i++
                    j++
                }
                oldLine != null && (newLine == null || !newLines.drop(j).take(3).any { it.trim() == oldLine.trim() }) -> {
                    result.add(DiffLine(DiffKind.REMOVED, i + 1, null, oldLine))
                    i++
                }
                newLine != null -> {
                    result.add(DiffLine(DiffKind.ADDED, null, j + 1, newLine))
                    j++
                }
            }
        }
        return result
    }
}
