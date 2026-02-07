document.addEventListener('DOMContentLoaded', function () {
  var mainslider = new Swiper('.heroBannerCarousel', {
    observer: true,  
    observeParents: true,
    loop: false,
    speed: 400,
    spaceBetween: 0,
    slidesPerView: 1, 
    hashNavigation: {
      watchState: true,
    },
    pagination: {
      el: ".swiper-pagination",
      clickable: true,
    },
  });


});





var swiperInstance; // To hold the Swiper instance

// Function to initialize or reinitialize Swiper
function initSwiper() {
  // Determine whether the slider should be vertical or horizontal based on screen width
  var isVertical = window.innerWidth > 992;
  
  // If a Swiper instance already exists, destroy it first
  if (swiperInstance) {
    swiperInstance.destroy(true, true); // Destroy and clean up
  }

  // Initialize a new Swiper instance with the appropriate direction
  swiperInstance = new Swiper('.categoryCarousel', {
    observer: true,
    observeParents: true,
    loop: false,
    speed: 400,
    spaceBetween: 0,
    slidesPerView: isVertical ? 4 : 4.5,
    direction: isVertical ? 'vertical' : 'horizontal', // Set direction based on screen width
    pagination: {
      el: '.swiper-pagination',
    },
    navigation: {
      nextEl: '.categoryCarouselNavNext',
      prevEl: '.categoryCarouselNavPrev',
    },
  });
}

// Initialize Swiper on page load
initSwiper();

// Add event listener to detect window resize and reinitialize Swiper
window.addEventListener('resize', function () {
  // Reinitialize Swiper only if the direction should change
  initSwiper();
});

  

document.addEventListener('DOMContentLoaded', function () {

var mainslider = new Swiper('.threeItmeCarousel', {
  observer: true,  
  observeParents: true,
  loop: true,
  speed: 400,
  spaceBetween: 12,
  slidesPerView: 1, 
  observer: true,  
  observeParents: true, 
  breakpoints: { 
    576: {
      slidesPerView: 1,
      spaceBetween: 12
    }, 
    992: {
      slidesPerView: 2,
      spaceBetween: 12
    }, 
    1200: {
      slidesPerView: 3,
      spaceBetween: 12
    }
  },
});

 
// Product slider script
var productCarousel = new Swiper('.productCarousel', {
  observer: true,  
  observeParents: true,
  loop: true,
  speed: 400,
  spaceBetween: 36,
  slidesPerView: 1.4,  
  centeredSlides: true,
  observer: true,  
  observeParents: true, 
  breakpoints: { 
    576: {
      slidesPerView: 1.4,
      spaceBetween: 12,
      centeredSlides: false
    }, 
    992: {
      slidesPerView: 2,
      spaceBetween: 12,
      centeredSlides: false
    }, 
    1200: {
      slidesPerView: 3,
      spaceBetween: 36,
      centeredSlides: false
    },
    1400: {
      slidesPerView: 4,
      spaceBetween: 36,
      centeredSlides: false
    }
  },
});

 
 

document.querySelectorAll('.category__item').forEach(item => {
  item.addEventListener('click', function() {
      const category = this.querySelector('h3').textContent.trim().toLowerCase(); // Get the category name (e.g., 'necklaces')
      const tab = document.getElementById(`${category}-tab`); // Find the corresponding tab by id

      document.querySelectorAll('.category__item').forEach(el => {
        el.classList.remove('active');
      });

      // Add 'active' class to the clicked category__item
      this.classList.add('active');
      
      if (tab) {
          var tabTrigger = new bootstrap.Tab(tab); // Bootstrap's tab switching method
          tabTrigger.show(); // Show the relevant tab
      }
  });
});




// gallery slider script
var galleryCarousel = new Swiper('.galleryCarousel', {
  observer: true,  
  observeParents: true,
  loop: true,
  speed: 400,
  spaceBetween: 36,
  slidesPerView: 1, 
  observer: true,  
  observeParents: true, 
  breakpoints: { 
    576: {
      slidesPerView: 1,
      spaceBetween: 12,
      centeredSlides: true
    }, 
    992: {
      slidesPerView: 1.4,
      spaceBetween: 12,
      centeredSlides: false
    }, 
    1200: {
      slidesPerView: 1.4,
      spaceBetween: 36,
      centeredSlides: false
    }
  },
  navigation: {
    nextEl: '.galleryCarouselNavBtnNext',
    prevEl: '.galleryCarouselNavBtnPrev',
  },
});


// Product slider script
var productThreeItemCarousel = new Swiper('.productThreeItemCarousel', {
  observer: true,  
  observeParents: true,
  loop: true,
  speed: 400,
  spaceBetween: 36,
  slidesPerView: 1.4, 
  centeredSlides: true,
  observer: true,  
  observeParents: true, 
  breakpoints: { 
    576: {
      slidesPerView: 2,
      spaceBetween: 12,
      centeredSlides: false
    }, 
    992: {
      slidesPerView: 2,
      spaceBetween: 12
    }, 
    1200: {
      slidesPerView: 3,
      spaceBetween: 36
    }
  },
});

// Product slider script
var productCarousel2 = new Swiper('.productCarousel2', {
  observer: true,  
  observeParents: true,
  loop: true,
  speed: 400,
  spaceBetween: 36,
  slidesPerView: 2.4, 
  observer: true,  
  observeParents: true, 
  breakpoints: { 
    576: {
      slidesPerView: 2,
      spaceBetween: 12, 
    }, 
    992: {
      slidesPerView: 3,
      spaceBetween: 12
    }, 
    1200: {
      slidesPerView: 3,
      spaceBetween: 36
    },
    1400: {
      slidesPerView: 4,
      spaceBetween: 36
    }
  },
  navigation: {
    nextEl: '.proNavBtnNext',
    prevEl: '.proNavBtnPrev',
  },
  pagination: {
    el: '.productCarousel2Pagination', 
  },
});
// Product slider script
var productCarousel3 = new Swiper('.productCarousel3', {
  observer: true,  
  observeParents: true,
  loop: true,
  speed: 400,
  spaceBetween: 36,
  slidesPerView: 2.4, 
  observer: true,  
  observeParents: true, 
  breakpoints: { 
    576: {
      slidesPerView: 2,
      spaceBetween: 12, 
    }, 
    992: {
      slidesPerView: 3,
      spaceBetween: 12
    }, 
    1200: {
      slidesPerView: 3,
      spaceBetween: 36
    },
    1400: {
      slidesPerView: 4,
      spaceBetween: 36
    }
  },
  navigation: {
    nextEl: '.proNavBtnNext',
    prevEl: '.proNavBtnPrev',
  },
  pagination: {
    el: '.productCarousel3Pagination', 
  },
});
// Product slider script
var productCarousel4 = new Swiper('.productCarousel4', {
  observer: true,  
  observeParents: true,
  loop: true,
  speed: 400,
  spaceBetween: 36,
  slidesPerView: 2.4, 
  observer: true,  
  observeParents: true, 
  breakpoints: { 
    576: {
      slidesPerView: 2,
      spaceBetween: 12, 
    }, 
    992: {
      slidesPerView: 3,
      spaceBetween: 12
    }, 
    1200: {
      slidesPerView: 3,
      spaceBetween: 36
    },
    1400: {
      slidesPerView: 4,
      spaceBetween: 36
    }
  },
  navigation: {
    nextEl: '.proNavBtnNext',
    prevEl: '.proNavBtnPrev',
  },
  pagination: {
    el: '.productCarousel4Pagination', 
  },
});


// Product slider script
var creationInnerCarousel = new Swiper('.creationInnerCarousel', {
  observer: true,  
  observeParents: true,
  loop: true,
  speed: 400,
  spaceBetween: 0,
  slidesPerView: 1, 
  observer: true,  
  observeParents: true, 
  pagination: {
    el: '.creationInnerCarouselPagination', 
  },
});

// Product slider script
var creationCarousel = new Swiper('.creationCarousel', {
  observer: true,  
  observeParents: true,
  loop: true,
  speed: 400,
  spaceBetween: 0,
  slidesPerView: 1, 
  observer: true,  
  observeParents: true,  
  navigation: {
    nextEl: '.creationCarouselNavBtnNext',
    prevEl: '.creationCarouselNavBtnPrev',
  },
});

$(document).ready(function() {
  $('.select').select2();

  $(".modal__select").select2({
    dropdownParent: $("#contactModal")
  });
});


// Filter Product slider script
var filterProductCarousel = new Swiper('.filterProductCarousel', {
  observer: true,  
  observeParents: true,
  loop: true,
  speed: 400,
  spaceBetween: 24,
  slidesPerView: 1.2, 
  observer: true,  
  observeParents: true, 
  breakpoints: { 
    576: {
      slidesPerView: 2,
      spaceBetween: 24, 
    }, 
    992: {
      slidesPerView: 2,
      spaceBetween: 24
    }, 
    1200: {
      slidesPerView: 3,
      spaceBetween: 24
    }
  }, 
});




var Slider = (function() {
  var initSlider = function() {
      var dir = $("html").attr("dir");
      var swipeHandler = new Hammer(document.getElementById("slider"));
      swipeHandler.on('swipeleft', function(e) {
          if (dir == "rtl")
              $(".arrow-left").trigger("click");
          else
              $(".arrow-right").trigger("click");
      });

      swipeHandler.on('swiperight', function(e) {
          if (dir == "rtl")
              $(".arrow-right").trigger("click");
          else
              $(".arrow-left").trigger("click");
      });

      $(".arrow-right , .arrow-left").click(function(event) {
          var nextActiveSlide = $(".slide.active").next();

          if ($(this).hasClass("arrow-left"))
              nextActiveSlide = $(".slide.active").prev();

          if (nextActiveSlide.length > 0) {
              var nextActiveIndex = nextActiveSlide.index();
              $(".dots span").removeClass("active");
              $($(".dots").children()[nextActiveIndex]).addClass("active");

              updateSlides(nextActiveSlide);
          }
      });

      $(".dots span").click(function(event) {
          var slideIndex = $(this).index();
          var nextActiveSlide = $($(".slider").children()[slideIndex]);
          $(".dots span").removeClass("active");
          $(this).addClass("active");

          updateSlides(nextActiveSlide);
      });

      var updateSlides = function(nextActiveSlide) {
          var nextActiveSlideIndex = $(nextActiveSlide).index();

          $(".slide").removeClass("prev-1");
          $(".slide").removeClass("next-1");
          $(".slide").removeClass("active");
          $(".slide").removeClass("prev-2");
          $(".slide").removeClass("next-2");

          nextActiveSlide.addClass("active");

          nextActiveSlide.prev().addClass("prev-1");
          nextActiveSlide.prev().prev().addClass("prev-2");
          nextActiveSlide.addClass("active");
          nextActiveSlide.next().addClass("next-1");
          nextActiveSlide.next().next().addClass("next-2");
      }

      var updateToNextSlide = function(nextActiveSlide)
      {
          
      }
  }
  return {
      init: function() {
          initSlider();
      }
  }
})();

$(function() {
  Slider.init();
});





// // Blog slider script
// var blogCarousel = new Swiper('.blogCarousel', {
//   observer: true,  
//   observeParents: true,
//   loop: true,
//   speed: 400,
//   spaceBetween: 20,
//   slidesPerView: 1, 
//   observer: true,  
//   centeredSlides: true,
//   observeParents: true, 
//   breakpoints: { 
//     576: {
//       slidesPerView: 1,
//       spaceBetween: 24, 
//     }, 
//     992: {
//       slidesPerView: 2,
//       spaceBetween: 24
//     }, 
//     1200: {
//       slidesPerView: 2.4,
//       spaceBetween: 24
//     }
//   }, 
//   pagination: {
//     el: '.blogCarouselPagination', 
//   },
// });

const Articalswiper = new Swiper('.blogCarousel', {
  // Optional parameters

  loop: true,
  initialSlide: 1,
  slidesPerView: 'auto',
  speed: 1000,
  //    effect:"fade-out",
  // autoplay: true,
  spaceBetween: 60,

  // If we need pagination

  navigation: {
    nextEl: '.blogCarouselNavBtnNext',
    prevEl: '.blogCarouselNavBtnPrev',
  },


});


// Blog slider script
var experienceCarousel = new Swiper('.experienceCarousel', {
  observer: true,  
  observeParents: true,
  loop: true,
  speed: 400,
  spaceBetween: 22,
  slidesPerView: 1, 
  observer: true,   
  observeParents: true, 
  breakpoints: { 
    576: {
      slidesPerView: 1,
      spaceBetween: 22, 
    }, 
    992: {
      slidesPerView: 2,
      spaceBetween: 22
    }, 
    1200: {
      slidesPerView: 3,
      spaceBetween: 22
    }
  }, 
  pagination: {
    el: '.experienceCarouselPagination', 
  },
});



// var cardCarousel = new Swiper(".cardCarousel", {
//   effect: "cards",
//       grabCursor: true,
// });
const opacitySwiper = new Swiper('.cardCarousel', {
  // Optional parameters
  // slidesPerView: 1,
  // loop: true,
  loop: true,
  slidesPerView: 'auto',
  effect: "cards",
  centeredSlides: true,
  initialSlide: 1, 
  breakpoints: {
      768: {
          slidesPerView: 'auto',

          cardsEffect: {
              perSlideOffset: 20,
              slideShadows: false,
              perSlideRotate: 8, // Rotation of cards in degrees
          },
      },
      0: {
          spaceBetween: 20,
          slidesPerView: 1,
          cardsEffect: {
              perSlideOffset: 20,
              slideShadows: false,
              perSlideRotate: 8, // Rotation of cards in degrees
          },
      }
  },
  spaceBetween: 0,



  // Navigation arrows
  navigation: {
      nextEl: '.swiper-button-next.opacitySwiper-n',
      prevEl: '.swiper-button-prev.opacitySwiper-p',
  },

  // And if we need scrollbar
  scrollbar: {
      el: '.cardCarouselPagination',
      clickable: true,
  },
});

});
