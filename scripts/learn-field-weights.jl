using JavaCall
using ArgParse

s = ArgParseSettings()
@add_arg_table s begin
    "--feature"
    help = "Features to use"
    action = :append_arg
end

parsed_args = parse_args(ARGS, s)

collections = ["SemSearch_ES", "ListSearch", "INEX_LD", "QALD2"]
folds = [1:5;]
features = parsed_args["feature"]
wd = pwd()
refs = RemoteRef[]

for feature = features
    mkpath("../field-weights/weights/$feature")
    for collection = collections
        for fold = folds
            r = @spawn (
                        cd(wd);
                        try
                        JavaCall.init(["-Djava.class.path=$(joinpath(wd, "target", "scala-2.10", "pfsdm-assembly-1.0.jar"))", "-ea", "-Djava.util.logging.config.file=./pfsdm-logging.properties"]);
                        end;
                        las = @jimport edu.wayne.pfsdm.auxiliary.LearnAndSave;
                        jcall(las, "main", Void, (Array{JString, 1},), ["../dbpedia-37-galago-paths.json", "dbpedia-er/dbpedia-37-galago-config.json", "dbpedia-er/field-weights-experiments/$feature.json", "dbpedia-er/nikita-queries-pfsdm/$collection.cv$fold.training.json", "dbpedia-er/features-scaling.json", "--outputParams=../field-weights/weights/$feature/$collection.cv$fold.json"])
            )
            push!(refs, r)
        end
    end
end

for ref = refs
    wait(ref)
end
