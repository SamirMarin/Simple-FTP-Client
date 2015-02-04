import System.Environment (getArgs)

interactWith function inputFile outputFile = do
    input <- readFile inputFile
    putStrLn (show (lines (function input)))
    putStrLn (head (words (function input)))
    writeFile outputFile (function input)

main = mainWith myFunction
    where mainWith function = do
            args <- getArgs
            case args of
                [input, output] -> interactWith function input output
                _               -> putStrLn "error: exactly two arguments needed"

            -- replace "id" with a function name
          myFunction = id
