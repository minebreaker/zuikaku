package rip.deadcode.zuikaku
package template

def renderJs(): String =
  s"""
(() => {
document.addEventListener("DOMContentLoaded", () => {
  const cellEl = document.querySelectorAll(".cell-image:not(.cell-image-link)")
  const modalEl = document.querySelector(".modal")
  const modalImageEl = document.querySelector(".modal-image")

  cellEl.forEach(e => {
    e.addEventListener("click", () => {
      modalEl.style.display = "flex"  // when "block" causes scrollbar problem and "flex" fixes it. Not sure why.
      console.log(document.body.scrollTop)
      modalEl.style.top = window.pageYOffset + "px"
      modalEl.classList.remove("modal-closed")

      modalImageEl.src = e.querySelector("img").src;
    })
  })

  modalEl?.addEventListener("click", () => {
    setTimeout(() => {
      modalEl.style.display = "none"
    }, 90)
    modalEl.classList.add("modal-closed")
  })
})
})()
"""
