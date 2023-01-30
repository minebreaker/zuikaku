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
      modalImageEl.src = e.querySelector("img").src;

      modalEl.style.display = "flex"  // when "block" causes scrollbar problem and "flex" fixes it. Not sure why.
      modalEl.style.top = window.pageYOffset + "px"
      modalEl.classList.remove("modal-closed")
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
