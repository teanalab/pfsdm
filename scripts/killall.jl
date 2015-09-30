workers = [1:nworkers();]

for worker = workers
    @spawnat worker run(`ps aux` |> `grep $(ARGS[1])` |> `awk '{print $2}'` |> `xargs kill`)
end
