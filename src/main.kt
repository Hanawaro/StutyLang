import core.LangCore

fun main(args: Array<String>) {

    var shouldCompile = false
    var shouldRun = false
    var path = ""

    for (arg in args) {
        if (arg == "-compile" || arg == "-c") {
            shouldCompile = true
            continue
        }

        if (arg == "-run" || arg == "-r") {
            shouldRun = true
            continue
        }

        if (arg == args.last()) {
            path = arg
            continue
        }

        println("Undefined key")
        return
    }

    if (path == "") {
        println("Undefined path")
        return
    }

    if (!shouldCompile && !shouldRun) {
        shouldCompile = true
        shouldRun = true
    }

    if (shouldCompile) {
        if (LangCore.compile(path)) {
            path = path.substring(0, path.lastIndexOf('.')) + ".bsl"

            if (shouldRun) {
                LangCore.run(path)
            }
        }
    }

    if (shouldRun && !shouldCompile) {
        LangCore.run(path)
    }
}