collections = ["SemSearch_ES", "ListSearch", "INEX_LD", "QALD2"]
folds = [1:5;]

for COLLECTION = collections
    for FOLD = folds
        @spawn (cd("$(homedir())/entity/pfsdm");
                ENV["JAVA_OPTS"]="-ea -Djava.util.logging.config.file=$(homedir())/entity/pfsdm-logging.properties";
                run(`sbt "runMain edu.wayne.pfsdm.auxiliary.LearnAndSave ../dbpedia-37-galago-paths.json dbpedia-er/dbpedia-37-galago-config.json dbpedia-er/learn-importance.json dbpedia-er/nikita-queries-wpfsdm/$COLLECTION.cv$FOLD.training.json dbpedia-er/features-scaling.json ../features/weights/learn-uni-fieldlikelihood-nnp.json/$COLLECTION.cv$FOLD.json ../features/weights/learn-bi-fieldlikelihood-baselinetopscore-np-part-nns.json/$COLLECTION.cv$FOLD.json --outputParams=../importance/weights/$COLLECTION.cv$FOLD.json"`))
        sleep(20)
    end
end
