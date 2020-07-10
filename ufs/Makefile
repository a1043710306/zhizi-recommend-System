# all: ufs gmp click
all: ufs reformat ufs_server report negative

# include ../mklove/Makefile.base
THRIFT=/usr/local/thrift-0.9.2
LIB_INVUTIL=../libinvutil
LIB_MONITOR=../libmonitor

RDINC = /usr/local/include/librdkafka/
INCLUDE = -I/usr/local/include -I$(LIB_INVUTIL)/include/ -I/usr/include/mysql/ -I/usr/local/include/hiredis -I/usr/local/thrift-0.9.2/include/ \
          -I./ -I./gen-cpp/ -I$(THRIFT)/include/thrift -I$(LIB_MONITOR)/include/

# CFLAGS += -gdwarf-2 -g -Wall -std=c++11 -I$(RDINC)
# CXXFLAGS += -g -Wall -std=c++11 -I$(RDINC)
#CFLAGS += -O3 -Wall -std=c11 -I$(RDINC)
#CXXFLAGS += -O3 -Wall -std=c++11 -I$(RDINC)
CFLAGS += -g -O2 -Wall -std=c++11 -I$(RDINC)
CXXFLAGS += -g -O2 -Wall -std=c++11 -I$(RDINC)
# LIBS = $(THRIFT)/lib/libthriftnb.a $(THRIFT)/lib/libthrift.a /usr/local/thrift-0.9.2/lib/libthrift.a ../../common/inutil/libinvutil.a /usr/local/lib/libevent.a \
# 		../../thirdparty/monitor/lib/libmonitor.a /usr/local/mysql/lib/libmysqlclient.a /usr/local/lib/libhiredis.a /usr/local/lib/librdkafka.a /usr/local/lib/libsasl2.a \
# 		/usr/lib64/libssl.a /usr/local/gcc-4.8.5/lib64/libstdc++.a -lcurl -lrt -pthread -lm
LIBS = $(THRIFT)/lib/libthriftnb.a $(THRIFT)/lib/libthrift.a /usr/local/thrift-0.9.2/lib/libthrift.a $(LIB_INVUTIL)/lib/libinvutil.a /usr/local/lib/libevent.a \
		$(LIB_MONITOR)/lib/libmonitor.a /usr/local/mysql/lib/libmysqlclient.a /usr/local/lib/libhiredis.a /usr/local/lib/librdkafka.a \
		/usr/lib64/libssl.a /usr/lib64/libcrypto.a /usr/local/gcc-4.8.5/lib64/libstdc++.a -lcurl -lrt -pthread -lm

OBJ = ./src/kafka_consumer.o \
	  ./src/db.o \
	  ./src/logic.o \
	  ./src/main.o \
	  ./src/click.o \
      ./src/ufs_logic.o \
      ./src/ufs_logic_reformat.o \
      ./src/util.o \
	  ./gen-cpp/UfsService.o \
	  ./src/reformat.o \
	  ./src/UfsServiceHandler.o \
	  ./src/UfsServiceLogic.o \
	  ./src/server.o \
	  ./src/click_log.o \
	  ./src/report.o \
	  ./src/ufs_in_mem.o \
	  ./src/ssdb_io.o \
	  ./src/ssdb_io_sync.o \
	  ./src/zhizi_define.o \
	  ./src/ufs_json_define.o \
	  ./src/event_parser.o \
	  ./src/common_weight.o \
	  ./src/negative_feedback.o \
      ./src/info_signal_cache.o

$(OBJ):%.o: %.cpp 
	$(CC) $(CPPFLAGS) $(CFLAGS) $(INCLUDE) -c $< -o $@ 

#gmp: ./src/kafka_consumer.o ./src/gmp.o ./src/es.o ./src/util.o ./src/notify_core.o ./gen-cpp/GmpKeyWord.o
#	$(CC) $(CPPFLAGS) $(CFLAGS) $(INCLUDE) $^ -o ./bin/$@ $(LDFLAGS) $(LIBS)

# sfu: ./src/kafka_consumer.o ./src/db.o ./src/user_profile.pb.o ./src/logic.o ./src/main.o
ufs: ./src/kafka_consumer.o ./src/db.o ./src/util.o ./src/ufs_logic_reformat.o \
	./src/ufs_logic.o ./src/main.o ./src/ufs_in_mem.o ./src/ssdb_io.o \
	./src/ssdb_io_sync.o ./src/zhizi_define.o \
	./src/ufs_json_define.o 
	$(CC) $(CPPFLAGS) $(CFLAGS) $(INCLUDE) $^ -o ./bin/$@ $(LDFLAGS) $(LIBS)

negative: ./src/kafka_consumer.o ./src/db.o ./src/util.o ./src/ssdb_io.o \
	./src/info_signal_cache.o ./src/event_parser.o ./src/common_weight.o \
	./src/negative_feedback.o 
	$(CC) $(CPPFLAGS) $(CFLAGS) $(INCLUDE) $^ -o ./bin/$@ $(LDFLAGS) $(LIBS)

reformat: ./src/kafka_consumer.o ./src/db.o ./src/util.o ./src/ufs_logic_reformat.o \
	./src/ufs_logic.o ./src/reformat.o ./src/ufs_in_mem.o ./src/ssdb_io.o \
	./src/ssdb_io_sync.o ./src/zhizi_define.o ./src/ufs_json_define.o
	$(CC) $(CPPFLAGS) $(CFLAGS) $(INCLUDE) $^ -o ./bin/$@ $(LDFLAGS) $(LIBS)

ufs_server: ./src/UfsServiceHandler.o ./src/UfsServiceLogic.o ./src/server.o ./src/db.o \
	./src/util.o ./src/ufs_logic_reformat.o ./src/ufs_logic.o  ./src/ufs_in_mem.o \
	./src/ssdb_io.o ./src/ssdb_io_sync.o ./src/zhizi_define.o ./src/zhizi_define.o \
	./src/ufs_json_define.o \
	./gen-cpp/UfsService_types.o ./gen-cpp/UfsService.o
	$(CC) $(CPPFLAGS) $(CFLAGS) $(INCLUDE) $^ -o ./bin/$@ $(LDFLAGS) $(LIBS)

report: ./src/report.o
	$(CC) $(CPPFLAGS) $(CFLAGS) $(INCLUDE) $^ -o ./bin/$@ $(LDFLAGS) $(LIBS)

# ./src/user_profile.pb.cpp: user_profile.proto
# 	protoc --cpp_out=./src user_profile.proto
# 	mv ./src/user_profile.pb.cc ./src/user_profile.pb.cpp

thrift: ../zhizi-thrift/src/main/thrift/UfsService.thrift
	    $(THRIFT)/bin/thrift -gen cpp $^

TESTFLAGS = -isystem $(GTEST_DIR)/include -isystem $(GMOCK_DIR)/include

GTEST_DIR = ../../thirdparty/googletest-master/googletest/
GMOCK_DIR = ../../thirdparty/googletest-master/googlemock/
USER_DIR = .

TESTS = ufs_unittest

GTEST_HEADERS = $(GTEST_DIR)/include/gtest/*.h \
				$(GTEST_DIR)/include/gtest/internal/*.h

GMOCK_HEADERS = $(GMOCK_DIR)/include/gmock/*.h \
				$(GMOCK_DIR)/include/gmock/internal/*.h \
				$(GTEST_HEADERS)

GTEST_SRCS_ = $(GTEST_DIR)/src/*.cc $(GTEST_DIR)/src/*.h $(GTEST_HEADERS)
GMOCK_SRCS_ = $(GMOCK_DIR)/src/*.cc $(GMOCK_HEADERS)

TESTDIR = test
$(TESTDIR)/gtest-all.o : $(GTEST_SRCS_)
	$(CXX) $(TESTFLAGS) -I$(GTEST_DIR) -I$(GMOCK_DIR) $(CXXFLAGS) -c \
		$(GTEST_DIR)/src/gtest-all.cc -o $(TESTDIR)/gtest-all.o

$(TESTDIR)/gmock-all.o : $(GMOCK_SRCS_)
	$(CXX) $(TESTFLAGS) -I$(GTEST_DIR) -I$(GMOCK_DIR) $(CXXFLAGS) \
		-c $(GMOCK_DIR)/src/gmock-all.cc -o $(TESTDIR)/gmock-all.o

$(TESTDIR)/gmock_main.o : $(GMOCK_SRCS_)
	$(CXX) $(TESTFLAGS) -I$(GTEST_DIR) -I$(GMOCK_DIR) $(CXXFLAGS) \
		-c $(GMOCK_DIR)/src/gmock_main.cc -o $(TESTDIR)/gmock_main.o

$(TESTDIR)/gmock.a : $(TESTDIR)/gmock-all.o $(TESTDIR)/gtest-all.o
	$(AR) $(ARFLAGS) $@ $^

$(TESTDIR)/gmock_main.a : $(TESTDIR)/gmock-all.o $(TESTDIR)/gtest-all.o $(TESTDIR)/gmock_main.o
	$(AR) $(ARFLAGS) $@ $^

TESTOBJS = $(TESTDIR)/test_helloworld.o \
		   $(TESTDIR)/test_jsonsax.o \
		   $(TESTDIR)/test_list.o \
		   $(TESTDIR)/test_linkedkvcache.o \
		   $(TESTDIR)/test_prioritykvcache.o \
		   $(TESTDIR)/test_uidhash.o \
		   $(TESTDIR)/test_eventparser.o \
		   $(TESTDIR)/test_ufsmerge.o \
		   $(TESTDIR)/event_parser.o \
		   $(TESTDIR)/util.o \
		   $(TESTDIR)/ufs_logic.o \
		   $(TESTDIR)/ufs_in_mem.o \
		   $(TESTDIR)/ufs_logic_reformat.o \
		   $(TESTDIR)/db.o \
		   $(TESTDIR)/ssdb_io.o \
		   $(TESTDIR)/ssdb_io_sync.o \
		   $(TESTDIR)/zhizi_define.o \
		   $(TESTDIR)/ufs_json_define.o \

# 		   $(TESTDIR)/test_libhttpclient.o \
# 		   $(TESTDIR)/test_libsharedqueue.o \
# 		   $(TESTDIR)/test_idiot.o \
# 		   $(TESTDIR)/test_libjson.o \
# 		   $(TESTDIR)/test_libqueue.o \
# 		   $(TESTDIR)/test_libcrypto.o \
# 		   $(TESTDIR)/test_libredisclient.o \
# 		   $(TESTDIR)/test_libstringhelper.o \
# 		   $(TESTDIR)/test_liboption.o \
# 		   $(TESTDIR)/test_libxmlparser.o \
# 		   $(TESTDIR)/test_liblog.o \
# 		   $(TESTDIR)/test_libdatetime.o


$(TESTDIR)/%.o : src/%.cpp
	$(CXX) $(CXXFLAGS) $(TESTFLAGS) $(INCLUDE) -o $@ -c $(filter %.cpp, $^)

$(TESTS) : $(TESTDIR) $(TESTOBJS) $(TESTDIR)/gmock_main.a
	$(CXX) $(TESTFLAGS) $(CXXFLAGS) $(filter %.o %.a, $^) $(LIBS) -o $@ /usr/local/gcc-4.8.5/lib64/libstdc++.a -pthread

unittest : $(TESTS)
	./$(TESTS)

$(TESTDIR) :
	mkdir -p $(TESTDIR)

clean:
	rm -f src/*.o
	rm -f ./bin/ufs
	rm -f ./bin/negative
	rm -f ./bin/reformat
	rm -f ./bin/ufs_server
	rm -f ./bin/report
	rm -fr gen-cpp
	rm -f $(TESTS) $(TESTDIR)/gmock.a $(TESTDIR)/gmock_main.a $(TESTDIR)/*.o
	rm -fr $(TESTDIR)

