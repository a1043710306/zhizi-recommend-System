package main

// import "log"
// import "fmt"
import "git.apache.org/thrift.git/lib/go/thrift"
import "github.com/BurntSushi/toml"
import "flag"
import "UfsService"
import "strconv"
import "os"
// import "tutorial"

type tomlConfig struct {
    ListenIp    string
    ListenPort  uint16
    // Ip          string
    // Port        uint16
    // CoolPadIp   string
    // CoolPadPort uint16
    CategoryHash    string
    TagHash         string
    // HashName    string
    DecayPeriod int
    DecayRate   float64
    Addrs       string
}

func main() {
    protocolFactory := thrift.NewTBinaryProtocolFactoryDefault()
    transportFactory := thrift.NewTFramedTransportFactory(thrift.NewTTransportFactory())

    var configFile string
	flag.StringVar(&configFile, "config", "./conf/config.conf", "Specify config file")
	flag.Parse()
    // fmt.Println(configFile)

    var config tomlConfig
    if _, err := toml.DecodeFile(configFile, &config); err != nil {
        // fmt.Println("toml.DecodeFile", err)
        // return
        panic(err)
    }

    // fmt.Println(config)
    // addr := "127.0.0.1:39090"

    addr := config.ListenIp + ":" + strconv.Itoa(int(config.ListenPort))
    // fmt.Println(addr)
    transport, err := thrift.NewTServerSocket(addr)
    if err != nil {
        return
    }

    f, err := os.OpenFile("log/UfsService.log", os.O_RDWR | os.O_CREATE | os.O_APPEND, 0666)
    if err != nil {
        panic(err)
    }
    defer f.Close()

    // handler := NewCalculatorHandler()
    // processor := tutorial.NewCalculatorProcessor(handler)
    handler := NewUfsServiceHandler(config.Addrs, config.CategoryHash, config.TagHash, f, config.DecayPeriod, config.DecayRate)
    processor := UfsService.NewUfsServiceProcessor(handler)
    server := thrift.NewTSimpleServer4(processor, transport, transportFactory, protocolFactory)
    server.Serve()
}

