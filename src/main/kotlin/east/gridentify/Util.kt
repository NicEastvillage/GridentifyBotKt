package east.gridentify

import java.util.concurrent.TimeUnit


fun formatTime(millis: Long): String {
    require(millis >= 0) { "Duration must be greater than zero!" }
    var remainingMillis = millis
    val days = TimeUnit.MILLISECONDS.toDays(remainingMillis)
    remainingMillis -= TimeUnit.DAYS.toMillis(days)
    val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis)
    remainingMillis -= TimeUnit.HOURS.toMillis(hours)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis)
    remainingMillis -= TimeUnit.MINUTES.toMillis(minutes)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis)
    remainingMillis -= TimeUnit.SECONDS.toMillis(seconds)
    val sb = StringBuilder(64)
    if (days > 0) {
        sb.append(days)
        sb.append("d ")
    }
    if (days > 0 || hours > 0) {
        sb.append(hours)
        sb.append("h ")
    }
    if (days > 0 || hours > 0 || minutes > 0) {
        sb.append(minutes)
        sb.append("m ")
    }
    if (days > 0 || hours > 0 || minutes > 0 || seconds > 0) {
        sb.append(seconds)
        sb.append("s ")
    }
    sb.append(remainingMillis)
    sb.append("ms")
    return sb.toString()
}