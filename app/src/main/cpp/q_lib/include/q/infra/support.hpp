/*=============================================================================
   Copyright (c) 2016-2023 Joel de Guzman

   Distributed under the MIT License [ https://opensource.org/licenses/MIT ]
=============================================================================*/
#if !defined(INFRA_SUPPORT_APRIL_10_2016)
#define INFRA_SUPPORT_APRIL_10_2016

#if defined(_WIN32)
# include <cmath>
#endif

#include <cfloat>
#include <cstdint>
#include <cassert>
#include <chrono>
#include <limits>
#include <climits>
#include <string>
#include <type_traits>

namespace cycfi
{
   ////////////////////////////////////////////////////////////////////////////
   // Metaprogramming utilities
   ////////////////////////////////////////////////////////////////////////////

   namespace detail
   {
      template <std::size_t bits>
      struct uint_that_fits_impl { using type = void; };

      template <>
      struct uint_that_fits_impl<8> { using type = std::uint8_t; };

      template <>
      struct uint_that_fits_impl<16> { using type = std::uint16_t; };

      template <>
      struct uint_that_fits_impl<32> { using type = std::uint32_t; };

      template <>
      struct uint_that_fits_impl<64> { using type = uint64_t; };

      constexpr std::size_t size_that_fits_int(std::size_t bits)
      {
         if (bits <= 8)
            return 8;
         else if (bits <= 16)
            return 16;
         else if (bits <= 32)
            return 32;
         else if (bits <= 64)
            return 64;
         return 0;
      }
   }

   template <std::size_t bits>
   struct uint_that_fits
     : detail::uint_that_fits_impl<detail::size_that_fits_int(bits)>
   {
      using type = typename
         detail::uint_that_fits_impl<detail::size_that_fits_int(bits)>::type;
#if !defined(_MSC_VER)
      static_assert(!std::is_same<type, void>::value,
         "Error: No int type fits specified number of bits."
      );
#endif
   };

   using natural_uint = typename uint_that_fits<sizeof(void*) * CHAR_BIT>::type;

   ////////////////////////////////////////////////////////////////////////////
   // Constants
	////////////////////////////////////////////////////////////////////////////
   template <typename T>
   struct int_traits
   {
      static constexpr T max = std::numeric_limits<T>::max();
      static constexpr T min = std::numeric_limits<T>::min();
   };

   template <typename T>
   constexpr T int_min()
   {
      return int_traits<T>::min;
   }

   ////////////////////////////////////////////////////////////////////////////
   // integer and binary functions
   ////////////////////////////////////////////////////////////////////////////

   // smallest power of 2 that fits n
   template <typename T>
   constexpr T smallest_pow2(T n, T m = 1)
   {
      return (m < n)? smallest_pow2(n, m << 1) : m;
   }
}
#endif
