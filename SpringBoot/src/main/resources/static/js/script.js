document.addEventListener("DOMContentLoaded", () => {
	// Theme toggle
	const themeToggle = document.getElementById("themeToggle")
	const htmlElement = document.documentElement

	const savedTheme = localStorage.getItem("theme")
	const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches

	if (savedTheme === "dark" || (!savedTheme && prefersDark)) {
		htmlElement.classList.add("dark-theme")
		if (themeToggle) {
			themeToggle.innerHTML = '<i class="fa-regular fa-sun"></i>'
		}
	}

	if (themeToggle) {
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
	}

	// Dropdown menu (account)
	const dropdownToggle = document.getElementById("dropdownToggle")
	const accountDropdown = dropdownToggle?.closest(".dropdown")

	if (dropdownToggle && accountDropdown) {
		dropdownToggle.addEventListener("click", (e) => {
			e.stopPropagation()
			accountDropdown.classList.toggle("active")
		})

		document.addEventListener("click", (e) => {
			if (!accountDropdown.contains(e.target)) {
				accountDropdown.classList.remove("active")
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

	// ✅ 관심사 선택 필터 (존재할 때만 동작하도록 수정)
	const inputContainer = document.getElementById("selected-interest-inputs")
	if (inputContainer) {
		document.querySelectorAll(".filter-btn").forEach(button => {
			button.addEventListener("click", function () {
				const id = this.dataset.id
				const existingInput = inputContainer.querySelector(`input[data-id="${id}"]`)
				const isActive = this.classList.toggle("active")

				if (isActive) {
					console.log("✅ 선택됨:", this.textContent.trim())

					if (!existingInput) {
						const input = document.createElement("input")
						input.type = "hidden"
						input.name = "interestIds"
						input.value = id
						input.dataset.id = id
						inputContainer.appendChild(input)
					}
				} else {
					console.log("❌ 해제됨:", this.textContent.trim())
					if (existingInput) {
						existingInput.remove()
					}
				}
			})
		})
	}

	// Notification dropdown toggle
	const notificationToggle = document.getElementById("notificationToggle")
	const notificationDropdown = notificationToggle?.closest(".dropdown")

	if (notificationToggle && notificationDropdown) {
		notificationToggle.addEventListener("click", (e) => {
			e.stopPropagation()
			notificationDropdown.classList.toggle("active")
		})

		document.addEventListener("click", (e) => {
			if (!notificationDropdown.contains(e.target)) {
				notificationDropdown.classList.remove("active")
			}
		})
	}
})
