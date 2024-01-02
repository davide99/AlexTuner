/*=============================================================================
   Copyright (c) 2014-2023 Joel de Guzman. All rights reserved.

   Distributed under the MIT License [ https://opensource.org/licenses/MIT ]
=============================================================================*/
#if !defined(CYCFI_Q_SUPPORT_BASE_HPP_DECEMBER_20_2015)
#define CYCFI_Q_SUPPORT_BASE_HPP_DECEMBER_20_2015

#include <type_traits>
#include <cstdint>
#include <cstring>
#include <algorithm>
#include "q/infra/support.hpp"

namespace cycfi::q {
    ////////////////////////////////////////////////////////////////////////////
    // Some macros
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // abs (we need ot here because std::abs may not be constexpr)
    ////////////////////////////////////////////////////////////////////////////
    template<typename T>
    constexpr T abs(T x) {
        return (x < T(0)) ? -x : x;
    }

    ////////////////////////////////////////////////////////////////////////////
    // rel_within
    ////////////////////////////////////////////////////////////////////////////
    inline bool rel_within(float a, float b, float eps) {
        return abs(a - b) <= eps * std::max(abs(a), abs(b));
    }
}

#endif
