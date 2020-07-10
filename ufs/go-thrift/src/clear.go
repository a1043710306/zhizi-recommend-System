
package main

import "time"
import "fmt"
import "os"
import "encoding/json"
// import "encoding/binary"
// import "crypto/md5"
import "ssdb"
import "strconv"
import "strings"

func getClearList(db* ssdb.Client) []string {
    var res []string
    resp, err := db.Do("hlist", "", "", 10000)
    if err != nil {
        fmt.Printf("hlist on ssdb fail", err)
    }
    // fmt.Println(resp)
    for i:= range(resp) {
        // fmt.Println(resp[i])
        if (strings.Index(resp[i], "zhizi.ufs.tag.v") >= 0) {
            res = append(res, resp[i])
        } else if (strings.Index(resp[i], "zhizi.ufs.category.v") >= 0) {
            res = append(res, resp[i])
        }
    }
    return res
}

type WeightedTag struct {
    Tag     string  `json:"tag"`
    Weight  float64 `json:"weight"`
}

type NewWeight struct {
    Ts      int  `json:"ts"`
    Weighted []WeightedTag   `json:"weighted"`
}

func delKey(db* ssdb.Client, hash string, field string) int {
    _, err := db.Do("hdel", hash, field);
    if err != nil {
        fmt.Println("hdel fail", err)
        return -1
    }
    return 0
}

func clearHash(db* ssdb.Client, hash string) (int, int) {
    start := ""
    end := ""
    limit := 10
    count := 0
    n := 0
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
            // fmt.Println(n, start)
            break;
        }
        n += len(resp)/2
        if n % 10000 < 10 {
            // fmt.Println(n, start)
        }
        for i := 2; i < len(resp); i += 2{
            uid := resp[i-1]
            start = uid
            var wt NewWeight
            json.Unmarshal([]byte(resp[i]), &wt)
            if (time.Now().Unix() - int64(wt.Ts) > 20*24*60*60) {
                if delKey(db, hash, uid) != 0 {
                    fmt.Println("hdel fail", uid)
                } else {
                    count += 1
                }
            }
            // else {
            //     fmt.Println(time.Now().Unix(), wt.Ts, time.Now().Unix() - int64(wt.Ts))
            // }
            // fmt.Println(wt.Ts)
        }
    }
    return n, count;
}

func main() {
    ip := os.Args[1]
    port, _:= strconv.Atoi(os.Args[2])

    db, err := ssdb.Connect(ip, port)
    if err != nil {
        panic(err)
    }

    defer db.Close()
    fmt.Println("begin: ", time.Now())
    hl := getClearList(db)
    for i := range(hl) {
        n, c := clearHash(db, hl[i])
        fmt.Println(hl[i], n, c)
    }
    fmt.Println("end: ", time.Now())
    // fmt.Println(getClearList(db))
}
