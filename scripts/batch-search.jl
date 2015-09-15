collections = ["SemSearch_ES", "ListSearch", "INEX_LD", "QALD2"]
folds = [1:5;]
wd = pwd()

weights = ARGS[1]
runs = ARGS[2]

for COLLECTION = collections
    for FOLD = folds
        @spawn (cd(wd);
                println("$weights/$COLLECTION.cv$FOLD.json");
                println("$runs/$COLLECTION.$FOLD.run");
                ENV["JAVA_OPTS"]="-ea -Djava.util.logging.config.file=pfsdm-logging.properties";
                run(`sbt --error "runMain org.lemurproject.galago.core.tools.App batch-search ../dbpedia-37-galago-paths.json dbpedia-er/dbpedia-37-galago-config.json dbpedia-er/nikita-queries-wpfsdm/$COLLECTION.cv$FOLD.test.json dbpedia-er/features-scaling.json ../features/weights/learn-uni-fieldlikelihood-nnp.json/$COLLECTION.cv$FOLD.json ../features/weights/learn-bi-fieldlikelihood-baselinetopscore-np-part-nns.json/$COLLECTION.cv$FOLD.json $weights/$COLLECTION.cv$FOLD.json"` |> `grep -v "\[.*success.*\]"` |> "$runs/$COLLECTION.$FOLD.run"))
        sleep(20)
    end
end
