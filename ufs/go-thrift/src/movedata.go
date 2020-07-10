package main

//import "time"
import "fmt"
import "os"
import "encoding/json"
import "encoding/binary"
import "crypto/md5"
import "ssdb"
import "strconv"
import "strings"

func GetSsdbConn(conns [](*ssdb.Client), uid string) (*ssdb.Client) {
    data := []byte(uid)
    sum := md5.Sum(data)
    module := len(conns)
    idx := binary.LittleEndian.Uint32(sum[:4])%uint32(module) //(sum[3] * 256 * 256 *256 + sunm[2] * 256 *256 + sum[1] * 256 + sum[0])%module
    fmt.Println("route uid", uid, idx)
    return conns[idx]
}

func hashSet(db* ssdb.Client, hash string, field string, value string) {
    fmt.Println("hset", hash, field)
    _, err := db.Do("hset", hash, field, value)
    if err != nil {
        fmt.Printf("field: %s hset fail", hash, field);
    }
}

type Profile struct {
    Age     int  `json:"age"`
    Gender  int  `json:"gender"`
    Mnc     int  `json:"mnc"`
    Ts      int  `json:"ts"`
    PurchasedUser  bool `json:"purchased_user"`
    Ufea    string  `json:"ufea"`
    WeightedTags   []VersionWeightedTag      `json:"weighted_tags"`
}

type VersionWeightedTag struct {
    Version string  `json:"version"`
    Tag     []WeightedTag   `json:"tag"`
}

type WeightedTag struct {
    Tag     string  `json:"tag"`
    Weight  float64 `json:"weight"`
}

type NewWeight struct {
    Ts      int  `json:"ts"`
    Weighted []WeightedTag   `json:"weighted"`
}

func hashScan(db* ssdb.Client, hash string, start string, limit int, conns []*ssdb.Client) {
    end := ""
    nc := 0
    outc := 0
    // conns := makeConn()
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
            uid := resp[i-1]
            fmt.Println("source uid:", uid, nc)
            var sp Profile
            json.Unmarshal([]byte(resp[i]), &sp)
            newConn := GetSsdbConn(conns, uid)
            for j := range(sp.WeightedTags) {
                version := sp.WeightedTags[j].Version
                var nw = NewWeight{Ts:sp.Ts, Weighted:sp.WeightedTags[j].Tag}
                benc, err := json.Marshal(nw)
                if err != nil {
                    panic(err)
                }
                if version[:3] == "cg_" {
                    vv := version[3:]
                    hashkey := "zhizi.ufs.category." + vv
                    hashSet(newConn, hashkey, uid, string(benc))
                } else if version[:3] == "tg_" {
                    vv := version[3:]
                    hashkey := "zhizi.ufs.tag." + vv
                    hashSet(newConn, hashkey, uid, string(benc))
                } else {
                    fmt.Println("unknown version", version)
                    os.Exit(1);
                }
            }
            start = resp[i-1]
        }
    }
}

func makeConn(addrs string) [](*ssdb.Client) {
    var res []*ssdb.Client
    arr := strings.Split(addrs, ",")
    for i := range(arr) {
        ep := strings.Split(arr[i], ":")
        port, err := strconv.Atoi(ep[1])
        if err != nil {
            panic(err)
        }
        db, err := ssdb.Connect(ep[0], port)
        if err != nil {
            panic(err)
        }
        res = append(res, db)
    }
    return res
}

func main() {
    ip := os.Args[1]
    port, _:= strconv.Atoi(os.Args[2])
    addrs := os.Args[3]

    conns := makeConn(addrs)
    db, err := ssdb.Connect(ip, port)
    if err != nil {
        panic(err)
    }

    defer db.Close()
    hashScan(db, "zhizi.ufs", "", 10, conns)
}
