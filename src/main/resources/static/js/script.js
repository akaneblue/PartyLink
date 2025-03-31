document.addEventListener("DOMContentLoaded", () => {
  // Theme toggle functionality
  const themeToggle = document.getElementById("themeToggle")
  const htmlElement = document.documentElement

  // Check for saved theme preference or respect OS preference
  const savedTheme = localStorage.getItem("theme")
  const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches

  if (savedTheme === "dark" || (!savedTheme && prefersDark)) {
    htmlElement.classList.add("dark-theme")
    themeToggle.innerHTML = '<i class="fa-regular fa-sun"></i>'
  }

  // Toggle theme when button is clicked
  themeToggle.addEventListener("click", () => {
    htmlElement.classList.toggle("dark-theme")

    if (htmlElement.classList.contains("dark-theme")) {
      localStorage.setItem("theme", "dark")
      themeToggle.innerHTML = '<i class="fa-regular fa-sun"></i>'
    } else {
      localStorage.setItem("theme", "light")
      themeToggle.innerHTML = '<i class="fa-regular fa-moon"></i>'
    }
  })

  // Dropdown menu functionality
  const dropdownToggle = document.getElementById("dropdownToggle")
  const dropdown = document.querySelector(".dropdown")

  if (dropdownToggle && dropdown) {
    dropdownToggle.addEventListener("click", (e) => {
      e.stopPropagation()
      dropdown.classList.toggle("active")
    })

    // Close dropdown when clicking outside
    document.addEventListener("click", (e) => {
      if (!dropdown.contains(e.target)) {
        dropdown.classList.remove("active")
      }
    })
  }

  // Mobile menu toggle
  const menuToggle = document.getElementById("menuToggle")
  const mainNav = document.querySelector(".main-nav")

  if (menuToggle && mainNav) {
    menuToggle.addEventListener("click", () => {
      mainNav.style.display = mainNav.style.display === "flex" ? "none" : "flex"
    })
  }

  // Filter buttons
  const filterButtons = document.querySelectorAll(".filter-btn")

  filterButtons.forEach((button) => {
    button.addEventListener("click", function () {
      // Remove active class from all buttons
      filterButtons.forEach((btn) => btn.classList.remove("active"))

      // Add active class to clicked button
      this.classList.add("active")

      // Here you would typically filter the content based on the selected category
      // For now, we'll just log the selected category
      console.log("Selected category:", this.textContent.trim())
    })
  })
})

