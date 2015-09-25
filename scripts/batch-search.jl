using JavaCall

collections = ["SemSearch_ES", "ListSearch", "INEX_LD", "QALD2"]
folds = [1:5;]
wd = pwd()
refs = RemoteRef[]

weights = ARGS[1]
runs = ARGS[2]

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
        jcall(galago, "run", Void, (Array{JString,1},ps), ["batch-search", "../dbpedia-37-galago-paths.json", "dbpedia-er/dbpedia-37-galago-config.json", "dbpedia-er/nikita-queries-wpfsdm/$collection.cv$fold.test.json", "dbpedia-er/features-scaling.json", "../features/weights/learn-uni-fieldlikelihood-nnp.json/$collection.cv$fold.json", "../features/weights/learn-bi-fieldlikelihood-baselinetopscore-np-part-nns.json/$collection.cv$fold.json", "$weights/$collection.cv$fold.json"],runps)
        )
        push!(refs, r)
    end
end

for ref = refs
    wait(ref)
end
