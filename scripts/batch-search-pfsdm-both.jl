using JavaCall
using ArgParse

s = ArgParseSettings()
@add_arg_table s begin
    "--weightsuni"
    help = "Unigrams features"
    "--weightsbi"
    help = "Bigrams features"
    "--weightslambdas"
    help = "Path to weights directory"
    "--runs"
    help = "Path to runs directory (output)"
end

parsed_args = parse_args(ARGS, s)

collections = ["SemSearch_ES", "ListSearch", "INEX_LD", "QALD2"]
folds = [1:5;]
wd = pwd()
refs = RemoteRef[]

weightsuni = parsed_args["weightsuni"]
weightsbi = parsed_args["weightsbi"]
weightslambdas = parsed_args["weightslambdas"]
runs = parsed_args["runs"]

mkpath(runs)

galago = @jimport org.lemurproject.galago.core.tools.App;
ps = @jimport java.io.PrintStream;

for collection = collections
    for fold = folds
        r = @spawn (
                    cd(wd);
                    try
                    JavaCall.init(["-Djava.class.path=$(joinpath(wd, "target", "scala-2.10", "pfsdm-assembly-1.0.jar"))", "-ea", "-Djava.util.logging.config.file=./pfsdm-logging.properties"]);
                    end;
                    galago = @jimport org.lemurproject.galago.core.tools.App;
                    ps = @jimport java.io.PrintStream;
        runps = ps((JString,), "$runs/$collection.$fold.run");
        jcall(galago, "run", Void, (Array{JString,1},ps), ["batch-search", "../dbpedia-37-galago-paths.json", "dbpedia-er/dbpedia-37-galago-config.json", "dbpedia-er/nikita-queries-pfsdm/$collection.cv$fold.test.json", "dbpedia-er/features-scaling.json", "$weightsuni/$collection.cv$fold.json", "$weightsbi/$collection.cv$fold.json", "$weightslambdas/$collection.cv$fold.json"],runps)
        )
        push!(refs, r)
    end
end

for ref = refs
    wait(ref)
end
