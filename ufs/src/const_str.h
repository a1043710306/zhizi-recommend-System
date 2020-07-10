/*************************************************************************
	> File Name: src/const_str.h
	> Author: xyz
	> Mail: xiao13149920@foxmail.com 
	> Created Time: Fri 18 May 2018 06:12:28 PM CST
 ************************************************************************/
#ifndef _CONST_STR_H_
#define _CONST_STR_H_
#include<stdexcept>
#include<string>

class StrLiter {
    public:
        template<size_t N>
            constexpr StrLiter(const char (&arr)[N]) :
                s(arr),
                str(s.c_str()),
                len(N-1) {
                }

        const char operator[](std::size_t n) const {
            return str[n];
        }

        const char at(std::size_t n) const {
            return n < len ? str[n]: throw std::out_of_range("");;
        }

        const char* c_str(void) const {
            return str;
        }

        const string& C_STR(void) const {
            return s;
        }

        size_t Length(void) const {
            return len;
        }

    private:
        const std::string s;
        const char* str;
        const size_t len;
};

#endif

