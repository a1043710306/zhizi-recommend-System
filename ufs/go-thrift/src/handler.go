package main

import (
    "log"
    "time"
    "os"
    "crypto/md5"
    "ssdb"
    "math"
    "encoding/json"
    "encoding/binary"
    "strings"
    "strconv"
)

type UfsServiceHandler struct {
    categoryHash    string
    tagHash         string
    conns           []*ssdb.Client
    logger*         log.Logger
    decayPeriod     int
    decayRate       float64
}

func (p* UfsServiceHandler) GetSsdbConn(uid string) (*ssdb.Client) {
    data := []byte(uid)
    sum := md5.Sum(data)
    module := len(p.conns)
    idx := binary.LittleEndian.Uint32(sum[:4])%uint32(module) 
    p.logger.Println("route uid", uid, idx)
    return p.conns[idx]
}

func MakeConns(addrs string) [](*ssdb.Client) {
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

func NewUfsServiceHandler(addrs string, categoryHash string, tagHash string, f *os.File,
    decayPeriod int, decayRate float64) *UfsServiceHandler {

    conns := MakeConns(addrs)
    logger := log.New(f, "", log.Ldate|log.Ltime|log.Lmicroseconds|log.Lshortfile)
	return &UfsServiceHandler{categoryHash, tagHash, conns, logger, decayPeriod, decayRate}
}

type Profile struct {
    // Age     int  `json:"age"`
    // Gender  int  `json:"gender"`
    // Mnc     int  `json:"mnc"`
    Ts      int  `json:"ts"`
    App     string `json:"app"`
    // PurchasedUser  bool `json:"purchased_user"`
    // Ufea    string  `json:"ufea"`
    // WeightedTags   []VersionWeightedTag      `json:"weighted_tags"`
    Weighted    []WeightedTag      `json:"weighted"`
}

// type VersionWeightedTag struct {
//     Version string  `json:"version"`
//     Tag     []WeightedTag   `json:"tag"`
// }

type WeightedTag struct {
    Tag     string  `json:"tag"`
    Weight  float64 `json:"weight"`
}

func (p* UfsServiceHandler) GetDecayFactor(lastUpdate int64) float64 {
    delta := time.Now().Unix() - lastUpdate
    return math.Pow(p.decayRate, float64(delta/int64(p.decayPeriod)))
}

func (p* UfsServiceHandler) DecayWeight(ori string) string {
    var sp Profile;
    json.Unmarshal([]byte(ori), &sp)
    // fmt.Println(sp)
    for i := range(sp.Weighted) {
        // vwt := &sp.WeightedTags[i]
        // for j := range(vwt.Tag) {
        wt := &sp.Weighted[i]
        wt.Weight *= p.GetDecayFactor(int64(sp.Ts))
        // }
    }
    b, err := json.Marshal(sp)
    if err != nil {
        panic(err)
    }
    return string(b)
}

func (p* UfsServiceHandler) GetWeightedTags(uid string, version string) (r string, err error) {
    p.logger.Println("enter GetWeightedTags", uid, version, p.tagHash)
    defer func() { p.logger.Println("leave GetWeightedTags") }()

    conn := p.GetSsdbConn(uid)
    resp, err := conn.HashGet(p.tagHash + version, uid)
    if err != nil || resp == nil{
        p.logger.Println("conn.HashGet fail", err, resp)
    }

    jsonStr, ok := resp.(string)
    if ok {
        jsonStr = p.DecayWeight(jsonStr)
        p.logger.Printf("%d bytes returned\n", len(jsonStr))
        return jsonStr, nil
    }
    p.logger.Println("error response")
    return "", nil
}

func (p* UfsServiceHandler) GetWeightedCategories(uid string, version string) (r string, err error) {
    p.logger.Println("enter GetWeightedCategories", uid, version, p.categoryHash)
    defer func() { p.logger.Println("leave GetWeightedCategories") }()

    conn := p.GetSsdbConn(uid)
    resp, err := conn.HashGet(p.categoryHash + version, uid)
    if err != nil || resp == nil{
        p.logger.Println("conn.HashGet fail", err, resp)
    }

    jsonStr, ok := resp.(string)
    if ok {
        jsonStr = p.DecayWeight(jsonStr)
        p.logger.Printf("%d bytes returned\n", len(jsonStr))
        return jsonStr, nil
    }
    p.logger.Println("error response")
    return "", nil
}

// go build -ldflags "-s" -gcflags "-N -l" -o ufs_server src/server.go src/handler.go
// go build -ldflags "-s" -gcflags "-N -l" -o ufs_client src/UfsService/ufs_service-remote/ufs_service-remote.go
