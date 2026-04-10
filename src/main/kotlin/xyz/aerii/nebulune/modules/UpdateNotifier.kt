package xyz.aerii.nebulune.modules

import com.google.gson.JsonArray
import xyz.aerii.athen.annotations.Priority
import xyz.aerii.athen.events.LocationEvent
import xyz.aerii.athen.events.core.on
import xyz.aerii.athen.handlers.Beacon.request
import xyz.aerii.athen.handlers.Chronos
import xyz.aerii.athen.handlers.Texter.onHover
import xyz.aerii.athen.handlers.Texter.onUrl
import xyz.aerii.athen.handlers.Typo.modMessage
import xyz.aerii.athen.ui.themes.Catppuccin
import xyz.aerii.library.api.mainThread
import xyz.aerii.library.handlers.parser.parse
import xyz.aerii.library.utils.showTitle
import xyz.aerii.nebulune.Nebulune
import kotlin.time.Duration.Companion.seconds

@Priority
object UpdateNotifier {
    private const val GITHUB_API = "https://api.github.com/repos/skies-starred/Nebulune/releases"
    private val versionRegex = Regex("""(\d+)\.(\d+)\.(\d+)(?:-r(\d+))?""") // https://regex101.com/r/An6dOq/1
    private var times: Int = 0
    private var latestVersion: Version? = null

    private data class Version(
        val major: Int,
        val minor: Int,
        val patch: Int,
        val revision: Int = -1,
        val tag: String
    ) : Comparable<Version> {
        override fun compareTo(other: Version): Int =
            compareValuesBy(this, other, { it.major }, { it.minor }, { it.patch }, { it.revision })

        fun display() = if (revision >= 0) "$major.$minor.$patch-r$revision" else "$major.$minor.$patch"
    }

    init {
        on<LocationEvent.Server.Connect> {
            if (times++ >= 3) return@on
            if (times == 1) {
                Chronos.schedule(5.seconds) { latest() }
                return@on
            }

            fn()
        }
    }

    private fun fn() {
        val current = Nebulune.modVersion.v() ?: return
        val latest = latestVersion?.takeIf { it > current } ?: return

        mainThread {
            "<aqua>Update available: <red>${latest.display()}".parse().showTitle()
            "<yellow>Update available for <${Catppuccin.Mocha.Green.argb}>Nebulune: <red>${current.display()} <gray>-> <aqua>${latest.display()}".parse()
                .onHover("<${Catppuccin.Mocha.Mauve.argb}>Click to view release!".parse())
                .onUrl("https://github.com/skies-starred/Nebulune/releases/tag/${latest.tag}")
                .modMessage()
        }
    }

    private fun String.v(): Version? {
        val match = versionRegex.find(this) ?: return null
        return Version(
            match.groupValues[1].toInt(),
            match.groupValues[2].toInt(),
            match.groupValues[3].toInt(),
            match.groupValues[4].toIntOrNull() ?: -1,
            this
        )
    }

    private fun latest() {
        GITHUB_API.request {
            onSuccess<JsonArray> { array ->
                latestVersion = array.mapNotNull { it.asJsonObject["tag_name"]?.asString?.v() }.maxOrNull()
                fn()
            }
        }
    }
}