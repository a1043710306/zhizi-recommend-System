package main

import "time"
import "fmt"
import "os"
import "strconv"
import "encoding/json"
import "./ssdb"

func hashDel(db* ssdb.Client, hash string, field string) {
    _, err := db.Do("hdel", hash, field)
    if err != nil {
        fmt.Printf("field: %s del fail", field);
    }
}

func hashScan(db* ssdb.Client, hash string, start string, limit int) {
    end := ""
    nc := 0
    outc := 0
    for {
        resp, err := db.Do("hscan", hash, start, end, limit)
        if err != nil {
            panic(err)
        }
        if len(resp)%2 != 1 {
            fmt.Println("bad response")
            os.Exit(-1);
        }
        if len(resp)/2 < limit {
            fmt.Println(nc, outc, start)
            break;
        }
        nc += len(resp)/2
        if nc % 10000 < 10 {
            fmt.Println(nc, outc, start)
        }
        for i := 2; i < len(resp); i += 2{
            // fmt.Println(resp[i])
            var f interface{}
            err := json.Unmarshal([]byte(resp[i]), &f)
            if err != nil {
                fmt.Println("json.Unmarshal fail")
                continue
            }
            m := f.(map[string]interface{})
            for k, v := range m {
                if k == "ts" {
                    ts, _ := v.(float64)
                    // fmt.Println(int64(ts))
                    if time.Now().Unix() - int64(ts) > 20*24*60*60{
                        outc += 1
                        hashDel(db, hash, resp[i-1])
                    }
                    // fmt.Println(v, time.Now().Unix())
                    break
                }
            }
            start = resp[i-1]
        }
    }
}

func main(){
    ip := os.Args[1]
    port, _:= strconv.Atoi(os.Args[2])

    db, err := ssdb.Connect(ip, port)
    if err != nil {
        panic(err)
    }

    defer db.Close()
    fmt.Println("begin: ", time.Now())
    hashScan(db, "zhizi.ufs", "", 10)
    fmt.Println("end: ", time.Now())

    // fmt.Println(resp[0], resp[1], resp[5], resp[3], resp[7])
}
