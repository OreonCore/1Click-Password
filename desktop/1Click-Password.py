import string
import secrets
import pyperclip

def generate_password():
    # Дефініція наборів символів
    lower_case = string.ascii_lowercase
    upper_case = string.ascii_uppercase
    digits = string.digits
    special_chars = "!@#$%^&*"

    # Генерація пароля
    password = [
        secrets.choice(lower_case),
        secrets.choice(upper_case),
        secrets.choice(digits),
        secrets.choice(special_chars),
    ]
    
    # Додаємо решту символів для довжини пароля 24
    all_chars = lower_case + upper_case + digits + special_chars
    password += [secrets.choice(all_chars) for _ in range(20)]

    # Перемішуємо пароль
    secrets.SystemRandom().shuffle(password)
    
    # Перетворюємо список в рядок
    password = ''.join(password)

    # Копіюємо пароль в буфер обміну
    pyperclip.copy(password)
    
    return password

if __name__ == "__main__":
    password = generate_password()
    print("Згенерований пароль:", password)
    print("Пароль скопійовано в буфер обміну.")
