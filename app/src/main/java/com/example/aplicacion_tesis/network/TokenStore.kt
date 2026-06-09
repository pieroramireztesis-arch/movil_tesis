package com.example.aplicacion_tesis.network

import android.content.Context
import android.content.SharedPreferences

/**
 * TokenStore:
 * Almacena y gestiona los datos de sesión del usuario
 * (token, id_usuario, rol, nombre, id_estudiante, id_docente, email)
 * usando SharedPreferences.
 */
object TokenStore {

    private const val PREFS_NAME = "auth_prefs"

    private const val KEY_TOKEN = "token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"   // 👈 NUEVO

    // Nuevas claves para distinguir entre estudiante y docente
    private const val KEY_STUDENT_ID = "student_id"
    private const val KEY_TEACHER_ID = "teacher_id"

    private lateinit var prefs: SharedPreferences

    /** Inicializa el almacenamiento de preferencias */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Guarda todos los datos del usuario autenticado.
     */
    fun save(
        tokenValue: String?,
        userIdValue: Int?,
        roleValue: String?,
        nameValue: String?,
        studentIdValue: Int? = null,
        teacherIdValue: Int? = null,
        emailValue: String? = null          // 👈 NUEVO (opcional, no rompe llamadas viejas)
    ) {
        if (!::prefs.isInitialized) return
        prefs.edit()
            .putString(KEY_TOKEN, tokenValue)
            .putInt(KEY_USER_ID, userIdValue ?: -1)
            .putString(KEY_USER_ROLE, roleValue)
            .putString(KEY_USER_NAME, nameValue)
            .putString(KEY_USER_EMAIL, emailValue)    // 👈 guardar email
            .putInt(KEY_STUDENT_ID, studentIdValue ?: -1)
            .putInt(KEY_TEACHER_ID, teacherIdValue ?: -1)
            .apply()
    }

    // =======================
    // GETTERS (lectura)
    // =======================

    val token: String?
        get() = if (::prefs.isInitialized) prefs.getString(KEY_TOKEN, null) else null

    val userId: Int?
        get() {
            if (!::prefs.isInitialized) return null
            val id = prefs.getInt(KEY_USER_ID, -1)
            return if (id == -1) null else id
        }

    val userRole: String?
        get() = if (::prefs.isInitialized) prefs.getString(KEY_USER_ROLE, null) else null

    val userName: String?
        get() = if (::prefs.isInitialized) prefs.getString(KEY_USER_NAME, null) else null

    val userEmail: String?
        get() = if (::prefs.isInitialized) prefs.getString(KEY_USER_EMAIL, null) else null

    val studentId: Int?
        get() {
            if (!::prefs.isInitialized) return null
            val id = prefs.getInt(KEY_STUDENT_ID, -1)
            return if (id == -1) null else id
        }

    val teacherId: Int?
        get() {
            if (!::prefs.isInitialized) return null
            val id = prefs.getInt(KEY_TEACHER_ID, -1)
            return if (id == -1) null else id
        }

    // =======================
    // SETTERS (actualización)
    // =======================

    fun setToken(value: String?) {
        if (!::prefs.isInitialized) return
        prefs.edit().putString(KEY_TOKEN, value).apply()
    }

    fun setUserId(value: Int?) {
        if (!::prefs.isInitialized) return
        prefs.edit().putInt(KEY_USER_ID, value ?: -1).apply()
    }

    fun setUserRole(value: String?) {
        if (!::prefs.isInitialized) return
        prefs.edit().putString(KEY_USER_ROLE, value).apply()
    }

    fun setUserName(value: String?) {
        if (!::prefs.isInitialized) return
        prefs.edit().putString(KEY_USER_NAME, value).apply()
    }

    fun setUserEmail(value: String?) {
        if (!::prefs.isInitialized) return
        prefs.edit().putString(KEY_USER_EMAIL, value).apply()
    }

    fun setStudentId(value: Int?) {
        if (!::prefs.isInitialized) return
        prefs.edit().putInt(KEY_STUDENT_ID, value ?: -1).apply()
    }

    fun setTeacherId(value: Int?) {
        if (!::prefs.isInitialized) return
        prefs.edit().putInt(KEY_TEACHER_ID, value ?: -1).apply()
    }

    // =======================
    // CLEAR (cerrar sesión)
    // =======================

    fun clear() {
        if (!::prefs.isInitialized) return
        prefs.edit().clear().apply()
    }
}
