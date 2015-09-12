workers = [1:nworkers();]

for worker = workers
    @spawnat worker run(`ps aux` |> `grep 'sbt'` |> `awk '{print $2}'` |> `xargs kill`)
end
