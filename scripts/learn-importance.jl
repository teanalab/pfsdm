using JavaCall

collections = ["SemSearch_ES", "ListSearch", "INEX_LD", "QALD2"]
folds = [1:5;]
features = ["title-binary", "title-presence"]
wd = pwd()
refs = RemoteRef[]

for feature = features
    for COLLECTION = collections
        for FOLD = folds
            r = @spawn (
                        cd(wd);
                        try
                        JavaCall.init(["-Djava.class.path=$(joinpath(wd, "target", "scala-2.10", "pfsdm-assembly-1.0.jar"))", "-ea", "-Djava.util.logging.config.file=../pfsdm-logging.properties"]);
                        end;
                        las = @jimport edu.wayne.pfsdm.auxiliary.LearnAndSave;
                        jcall(las, "main", Void, (Array{JString, 1},), ["../dbpedia-37-galago-paths.json", "dbpedia-er/dbpedia-37-galago-config.json", "dbpedia-er/importance-experiments/learn-importance-collectiontf-$feature.json", "dbpedia-er/nikita-queries-wpfsdm/$COLLECTION.cv$FOLD.training.json", "dbpedia-er/features-scaling.json", "../features/weights/learn-uni-fieldlikelihood-nnp.json/$COLLECTION.cv$FOLD.json", "../features/weights/learn-bi-fieldlikelihood-baselinetopscore-np-part-nns.json/$COLLECTION.cv$FOLD.json", "--outputParams=../importance/weights/$feature/$COLLECTION.cv$FOLD.json"])
            )
            push!(refs, r)
        end
    end
end

for ref = refs
    wait(ref)
end
