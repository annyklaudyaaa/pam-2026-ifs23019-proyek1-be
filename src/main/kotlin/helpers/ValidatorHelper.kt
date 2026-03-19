package org.delcom.helpers

import org.delcom.data.AppException
import java.util.regex.Pattern

class ValidatorHelper(private val data: Map<String, Any?>) {

    // Kita gunakan List untuk menampung pesan error
    val errors = mutableListOf<String>()

    fun addError(field: String, error: String) {
        errors.add("$field: $error")
    }

    // TAMBAHKAN ': ValidatorHelper' dan 'return this' di setiap fungsi
    fun required(field: String, message: String? = null): ValidatorHelper {
        val value = data[field]
        if (value == null || (value is String && value.isBlank())) {
            addError(field, message ?: "$field is required")
        }
        return this
    }

    fun minLength(field: String, min: Int, message: String? = null): ValidatorHelper {
        val value = data[field]
        if (value is String && value.length < min) {
            addError(field, message ?: "$field must be at least $min characters")
        }
        return this
    }

    fun maxLength(field: String, max: Int, message: String? = null): ValidatorHelper {
        val value = data[field]
        if (value is String && value.length > max) {
            addError(field, message ?: "$field must be at most $max characters")
        }
        return this
    }

    fun email(field: String, message: String? = null): ValidatorHelper {
        val value = data[field]
        if (value is String) {
            val pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
            if (!pattern.matcher(value).matches()) {
                addError(field, message ?: "$field must be a valid email")
            }
        }
        return this
    }

    // Fungsi final untuk melempar exception jika ada error
    fun validate() {
        if (errors.isNotEmpty()) {
            // Kita gabungkan error dengan pemisah '|' agar mudah di-split di Android nanti
            throw AppException(400, "Data tidak valid!", errors.joinToString("|"))
        }
    }
}