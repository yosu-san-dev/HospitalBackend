const API_BASE_URL = "https://hospitalbackend-dgad.onrender.com";

// Helper: Checks if the JWT token is expired
function isTokenExpired(token) {
    if (!token) return true;
    try {
        // JWTs have 3 parts split by dots. Part 2 is the payload.
        const payloadBase64 = token.split('.')[1];
        const decodedJson = atob(payloadBase64);
        const payload = JSON.parse(decodedJson);
        
        // "exp" is in seconds, Date.now() is in milliseconds
        const expirationTime = payload.exp * 1000; 
        return Date.now() > expirationTime;
    } catch (e) {
        return true; // If we can't read it, assume it's bad
    }
}

// --- STATE MANAGEMENT ---
let currentUser = null;
let selectedSlot = null;
let currentMonth = new Date().getMonth();
let currentYear = new Date().getFullYear();

// Simple in-memory storage for reservations (Reset on refresh)
// Format: { date: "2023-11-20", time: "09:00 AM", doctor: "Dr. Smith", dept: "Cardiology" }
let reservations = []; 

// --- NAVIGATION ---
function showPage(pageId) {
    document.querySelectorAll('.page').forEach(page => page.classList.remove('active'));
    document.getElementById(pageId).classList.add('active');
}

function updateNav() {
    // 1. Check if we have a token in the browser's "pocket" (localStorage)
    const token = localStorage.getItem('jwt_token');
    // If token exists and isn't empty, we are logged in
    const isLoggedIn = token !== null && token !== "";

    document.getElementById('nav-booking').style.display = isLoggedIn ? 'inline' : 'none';
    document.getElementById('nav-calendar').style.display = isLoggedIn ? 'inline' : 'none';
    document.getElementById('nav-logout').style.display = isLoggedIn ? 'inline' : 'none';
}

// --- AUTHENTICATION ---
async function handleLogin(e) {
    e.preventDefault();

    // 1. Get values from the new HTML IDs
    const tcInput = document.getElementById('login-tc');
    const passInput = document.getElementById('login-pass');

    const credentials = {
        tc: tcInput.value,
        password: passInput.value
    };

    try {
        console.log("Attempting login for TC:", credentials.tc);

        // 2. Send to Backend
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(credentials)
        });

        // 3. Get the result
        // The backend returns the raw token string (or an error message)
        const result = await response.text();

        if (result.includes("Failed") || result.includes("Error") || !response.ok) {
            // Login failed
            console.log("❌ " + result);
        } else {
            // SUCCESS! "result" is our JWT Token.
            // 4. Save the token in Chrome's localStorage
            localStorage.setItem('jwt_token', result);
            
            // Optional: Save the TC so we know who is logged in
            localStorage.setItem('user_tc', credentials.tc); 

            console.log("✅ Login Successful!");

            // 5. Update the UI
            updateNav();
            showPage('booking-page');
            
            // Load the doctors immediately
            if(typeof initBookingPage === "function") {
                initBookingPage(); 
            }
        }

    } catch (error) {
        console.error("Login Error:", error);
        console.log("⚠️ Connection Error. Is the backend running?");
    }
}

// --- AUTHENTICATION ---

async function handleRegister(e) {
    e.preventDefault(); // Stop page refresh

    // 1. Get the values from HTML
    const nameInput = document.getElementById('reg-name');
    const tcInput = document.getElementById('reg-tc');
    const passInput = document.getElementById('reg-pass');

    // 2. Prepare data for Java (Keys must match AuthController.java)
    const userData = {
        fullName: nameInput.value,
        tc: tcInput.value,
        password: passInput.value
    };

    console.log("Sending registration data:", userData); // Debug log

    try {
        // 3. Send POST request to Backend
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        });

        // 4. Read the text response from Java
        const message = await response.text();
        console.log("Backend response:", message);

        // 5. Check if it worked
        if (message.includes("successful")) {
            console.log("✅ " + message);
            
            // Clear the boxes
            nameInput.value = "";
            tcInput.value = "";
            passInput.value = "";
            
            // Optional: You could switch to login view here if you want
            // showPage('auth-page'); 

        } else {
            // Java sent an error (like "TC already exists")
            console.log("❌ " + message);
        }

    } catch (error) {
        console.error("Registration Error:", error);
        alert("⚠️ Connection Failed! Is the Java Backend running?");
    }
}

// Update Logout to clear the token
function logout() {
    // Remove the keys from the "pocket"
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_tc');
    
    // Reset internal state
    currentUser = null;
    reservations = []; 
    
    updateNav();
    showPage('auth-page');
}

// --- BOOKING LOGIC ---

// Global variable to store doctor data
let allDoctors = []; 

async function initBookingPage() {
    // 1. Set default date to today
    const dateInput = document.getElementById('date-select');
    if (dateInput) {
        const today = new Date().toISOString().split('T')[0];
        dateInput.value = today;
        // Prevent picking past dates
        dateInput.min = today; 
    }
    
    // 2. Setup visual time slots
    generateTimeSlots();

    // 3. Fetch Real Doctors
    const deptSelect = document.getElementById('dept-select');
    const docSelect = document.getElementById('doc-select');
    
    docSelect.innerHTML = '<option>Loading...</option>';

    try {
        const response = await fetch(`${API_BASE_URL}/doctors/all`);
        if (!response.ok) throw new Error("Failed to load doctors");

        // Save to global variable
        allDoctors = await response.json();
        console.log("Doctors loaded:", allDoctors);

        // 4. Initialize the Dropdowns
        populateDepartments();
        
        // Add event listener: When Dept changes, update Doctors
        deptSelect.addEventListener('change', filterDoctors);
        
        // Trigger once to fill initial state
        filterDoctors();

    } catch (error) {
        console.error("Error:", error);
        docSelect.innerHTML = '<option>Error loading lists</option>';
    }
}

function populateDepartments() {
    const deptSelect = document.getElementById('dept-select');
    
    // 1. Extract unique branches from the doctor list
    // This Set magic removes duplicates automatically
    const uniqueBranches = [...new Set(allDoctors.map(doc => doc.branch))];
    
    // 2. Clear and Fill the dropdown
    deptSelect.innerHTML = '';
    
    uniqueBranches.forEach(branch => {
        const option = document.createElement('option');
        option.value = branch;
        option.textContent = branch;
        deptSelect.appendChild(option);
    });
}

function filterDoctors() {
    const deptSelect = document.getElementById('dept-select');
    const docSelect = document.getElementById('doc-select');
    
    // 1. Get the currently selected department
    const selectedDept = deptSelect.value;
    
    // 2. Filter the global list
    const filteredDocs = allDoctors.filter(doc => doc.branch === selectedDept);
    
    // 3. Update the Doctor Dropdown
    docSelect.innerHTML = ''; // Clear old options
    
    filteredDocs.forEach(doc => {
        const option = document.createElement('option');
        option.value = doc.id; // VALUE is the database ID (e.g., 659a...)
        option.textContent = doc.name; // TEXT is the name (e.g., Ahmet Yilmaz)
        docSelect.appendChild(option);
    });
}

function generateTimeSlots() {
    const container = document.getElementById('slots-grid');
    container.innerHTML = '';
    
    // Simple fixed hours for now
    const times = ["09:00", "10:00", "11:00", "13:00", "14:00", "15:00"];
    
    times.forEach(time => {
        const btn = document.createElement('div');
        btn.className = 'slot';
        btn.textContent = time; // "09:00"
        btn.onclick = () => selectSlot(btn, time);
        container.appendChild(btn);
    });
}

function selectSlot(element, time) {
    document.querySelectorAll('.slot').forEach(s => s.classList.remove('selected'));
    element.classList.add('selected');
    selectedSlot = time; // Stores "09:00"
}

async function confirmBooking() {
    // 1. Validation
    if (!selectedSlot) {
        console.log("Please select a time slot.");
        return;
    }
    
    const docId = document.getElementById('doc-select').value;
    const dateStr = document.getElementById('date-select').value; // "2026-05-20"
    
    // 2. Create the exact Format Java needs (ISO 8601)
    // We combine Date + Time + Seconds
    const startTime = `${dateStr}T${selectedSlot}:00`; 
    
    // Calculate End Time (Start + 1 Hour)
    // We cheat a bit with string manipulation for simplicity
    let startHour = parseInt(selectedSlot.split(':')[0]);
    let endHour = startHour + 1;
    // Format it back to "10:00" (add leading zero if needed)
    let endHourStr = endHour < 10 ? "0" + endHour : endHour;
    const endTime = `${dateStr}T${endHourStr}:00:00`;

    // 3. Prepare the Data Packet
    const bookingData = {
        doctorId: docId,
        startTime: startTime,
        endTime: endTime
    };

    console.log("Sending Reservation:", bookingData);

    try {
        const token = localStorage.getItem('jwt_token');

        // 4. Send POST request
        const response = await fetch(`${API_BASE_URL}/reservation/create`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}` // IMPORTANT: Send the key!
            },
            body: JSON.stringify(bookingData)
        });

        // ... inside confirmBooking try block ...

        const message = await response.text();

        // ERROR HANDLING UPGRADE
        if (message.includes("confirmed")) {
            console.log("✅ " + message);
            selectedSlot = null;
            generateTimeSlots();
        } else if (message.includes("Token") || message.includes("expired") || response.status === 403) {
            // The Backend rejected us!
            console.log("⚠️ Session Expired. Please login again.");
            logout(); // Kick the user out immediately
        } else {
            console.log("❌ Booking Failed: " + message);
        }

// ... catch block remains same ...

    } catch (error) {
        console.error("Booking Error:", error);
        console.log("⚠️ Connection Error");
    }
}

// --- INITIALIZATION (Run when page loads) ---
document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('jwt_token');

    // CHECK 1: Is the token expired?
    if (token && isTokenExpired(token)) {
        console.log("Token expired! Logging out...");
        logout(); // This clears the storage and resets the view
        return; // Stop here
    }

    // CHECK 2: Update UI
    updateNav();

    if (token) {
        // ✅ Valid Token Found
        console.log("Welcome back! Token is valid.");
        showPage('booking-page');
        
        if (typeof initBookingPage === "function") {
            initBookingPage();
        }
    } else {
        // ❌ No Token or Logged Out
        showPage('auth-page');
    }
});

// --- CALENDAR LOGIC ---

// Helper: Fetch reservations from backend
async function fetchUserReservations() {
    try {
        const token = localStorage.getItem('jwt_token');
        if (!token) return [];

        // Note: Your Controller uses POST for retrieving appointments
        const response = await fetch(`${API_BASE_URL}/reservation/my-appointments`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const data = await response.json();
            console.log("Fetched Reservations:", data);
            
            // Update global state
            reservations = data;
            
            // Update the UI list immediately
            updateReservationList();
            return data;
        }
    } catch (error) {
        console.error("Error fetching reservations:", error);
    }
    return [];
}

// 1. Change Month Button Logic
function changeMonth(step) {
    currentMonth += step;
    if (currentMonth > 11) {
        currentMonth = 0;
        currentYear++;
    } else if (currentMonth < 0) {
        currentMonth = 11;
        currentYear--;
    }
    renderCalendar();
}

// 2. Main Calendar Render Logic
async function renderCalendar() {
    const display = document.getElementById('month-year-display');
    const grid = document.getElementById('calendar-days');
    const monthNames = ["Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"];
    
    display.textContent = `${monthNames[currentMonth]} ${currentYear}`;
    grid.innerHTML = '';

    // First, make sure we have the latest data
    // (If we haven't loaded doctors yet, load them now so we can show names)
    if (allDoctors.length === 0) {
        try {
            const docResp = await fetch(`${API_BASE_URL}/doctors/all`);
            allDoctors = await docResp.json();
        } catch(e) { console.log("Could not load doctor names"); }
    }

    // Fetch latest reservations for the logged-in user
    await fetchUserReservations();

    // Logic to get days in month
    const firstDay = new Date(currentYear, currentMonth, 1).getDay();
    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();

    // Empty slots for previous month (visual spacer)
    for (let i = 0; i < firstDay; i++) {
        const empty = document.createElement('div');
        grid.appendChild(empty);
    }

    // Draw the Days
    for (let day = 1; day <= daysInMonth; day++) {
        const dayCell = document.createElement('div');
        dayCell.className = 'day';
        dayCell.textContent = day;

        // Create date string to match backend (YYYY-MM-DD)
        // Note: Month is 0-indexed, so we add 1. PadStart adds leading '0' (e.g., 5 -> 05).
        const dateString = `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        
        // CHECK: Do we have a reservation on this exact date?
        // Backend format: "2026-05-20T09:00:00" -> We check if it starts with "2026-05-20"
        const myRes = reservations.find(r => r.startDT.startsWith(dateString));
        
        if (myRes) {
            // ✅ FOUND A RESERVATION
            const badge = document.createElement('span');
            badge.className = 'badge booked';
            
            // Format time: "2026-05-20T09:00:00" -> "09:00"
            const timePart = myRes.startDT.split('T')[1].substring(0, 5);
            badge.textContent = timePart; 
            
            dayCell.appendChild(badge);
            dayCell.style.backgroundColor = "#e0f2fe"; // Light blue highlight
        }

        grid.appendChild(dayCell);
    }
}

// 3. Update the List below the calendar
function updateReservationList() {
    const list = document.getElementById('appointment-list');
    list.innerHTML = '';

    if (!reservations || reservations.length === 0) {
        list.innerHTML = '<li>No appointments found.</li>';
        return;
    }

    reservations.forEach(r => {
        const li = document.createElement('li');
        
        // Parse Date and Time
        const [datePart, timePart] = r.startDT.split('T');
        
        // Find Doctor Name (using the ID from the reservation)
        const doctorObj = allDoctors.find(d => d.id === r.doctorId);
        const doctorName = doctorObj ? `${doctorObj.name} (${doctorObj.branch})` : "Unknown Doctor";

        li.innerHTML = `
            <strong>${datePart}</strong> at <strong>${timePart.substring(0,5)}</strong> <br>
            👨‍⚕️ ${doctorName}
        `;
        li.style.marginBottom = "10px";
        li.style.paddingBottom = "10px";
        li.style.borderBottom = "1px solid #eee";
        
        list.appendChild(li);
    });
}

// Override showPage to auto-refresh calendar when opened
const originalShowPage = showPage;
showPage = function(pageId) {
    originalShowPage(pageId);
    if(pageId === 'calendar-page') {
        renderCalendar();
    }
}

// Initialize
updateNav();

// Initialize
updateNav();
