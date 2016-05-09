+function () {
  var intervals = []

  intervals.push(setInterval(function () {
    $('.umbel-logo .animated').each(function () {
      this.classList.remove('animated')
      this.offsetHeight
      this.classList.add('animated')
    })
  }, 2000))


  var appearPhase = true;
  var $text = $('.loading-text')

  var togglePhase = function () {
    appearPhase = !appearPhase
  }

  intervals.push(setInterval(function () {
    var selector = appearPhase ? '.letter:not(.visible)' : '.letter.visible'
    var $letters = $text.find(selector)

    $letters
      .eq(~~(Math.random() * $letters.length))
      .toggleClass('visible', appearPhase)

    $letters.length - 1 || setTimeout(togglePhase, 2000)
  }, 300))
  
  function destroy() {
    $('.loading-splash').remove()
    intervals.forEach(function(interval) {
      clearInterval(interval)
    })
  }
  
  $(window).on('loadComplete.um', destroy)
}()