workers = [1:nworkers();]

for worker = workers
    @spawnat worker run(`ps aux` |> `grep 'julia'` |> `awk '{print $2}'` |> `xargs kill`)
end
