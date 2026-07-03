import { useState } from 'react'
import { jwtDecode } from 'jwt-decode'
import Login from './pages/Login'
import Register from './pages/Register'
import PatientDashboard from './pages/PatientDashboard'
import DoctorDashboard from './pages/DoctorDashboard'
import AdminDashboard from './pages/AdminDashboard'

export default function App() {
  const [token, setToken] = useState(localStorage.getItem('token'))
  const [page, setPage] = useState('login')

  const handleLogin = (token) => {
    setToken(token)
  }

  const handleLogout = () => {
    localStorage.removeItem('token')
    setToken(null)
    setPage('login')
  }

  let user = null
  let role = null
  if (token) {
    try {
      user = jwtDecode(token)
      role = user.role
    } catch (e) {
      localStorage.removeItem('token')
    }
  }

  if (!token) {
    return (
        <div>
          <div style={styles.nav}>
            <span style={styles.logo}>🏥 HealthCare</span>
            <div>
              <button style={styles.navBtn} onClick={() => setPage('login')}>Login</button>
              <button style={styles.navBtn} onClick={() => setPage('register')}>Register</button>
            </div>
          </div>
          {page === 'login' ? <Login onLogin={handleLogin} /> : <Register />}
        </div>
    )
  }

  return (
      <div style={{ background:'#f0f4f8', minHeight:'100vh' }}>
        <div style={styles.nav}>
          <span style={styles.logo}>🏥 HealthCare</span>
          <div>
            <span style={styles.welcome}>Welcome, {user?.sub}</span>
            <button style={styles.navBtn} onClick={handleLogout}>Logout</button>
          </div>
        </div>
        {role === 'PATIENT' && <PatientDashboard userId={user?.id} />}
        {role === 'DOCTOR' && <DoctorDashboard userId={user?.id} />}
        {role === 'ADMIN' && <AdminDashboard />}
        {!role && <p style={{padding:'2rem'}}>Unknown role. Please login again.</p>}
      </div>
  )
}

const styles = {
  nav: { display:'flex', justifyContent:'space-between', alignItems:'center',
    padding:'1rem 2rem', background:'white',
    boxShadow:'0 2px 8px rgba(0,0,0,0.08)' },
  logo: { fontSize:'1.25rem', fontWeight:'700', color:'#2d3748' },
  navBtn: { padding:'0.5rem 1rem', margin:'0 0.25rem', background:'#4299e1',
    color:'white', border:'none', borderRadius:'6px', cursor:'pointer' },
  welcome: { marginRight:'1rem', color:'#4a5568' }
}