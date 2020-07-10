package main

// import "fmt"
import "git.apache.org/thrift.git/lib/go/thrift"
import "tutorial"

func main() {
    protocolFactory := thrift.NewTBinaryProtocolFactoryDefault()
    transportFactory := thrift.NewTFramedTransportFactory(thrift.NewTTransportFactory())

    addr := "127.0.0.1:39090"
    transport, err := thrift.NewTServerSocket(addr)
    if err != nil {
        return
    }

    handler := NewCalculatorHandler()
    processor := tutorial.NewCalculatorProcessor(handler)
    server := thrift.NewTSimpleServer4(processor, transport, transportFactory, protocolFactory)
    server.Serve()
}
