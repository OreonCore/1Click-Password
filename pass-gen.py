import random
import string
import pyperclip

def generate_password():
    # Дефініція наборів символів
    lower_case = string.ascii_lowercase
    upper_case = string.ascii_uppercase
    digits = string.digits
    special_chars = "!@#$%^&*"

    # Генерація пароля
    password = [
        random.choice(lower_case),
        random.choice(upper_case),
        random.choice(digits),
        random.choice(special_chars),
    ]
    
    # Додаємо решту символів для довжини пароля 24
    all_chars = lower_case + upper_case + digits + special_chars
    password += random.choices(all_chars, k=20)

    # Перемішуємо пароль
    random.shuffle(password)
    
    # Перетворюємо список в рядок
    password = ''.join(password)

    # Копіюємо пароль в буфер обміну
    pyperclip.copy(password)
    
    return password

if __name__ == "__main__":
    password = generate_password()
    print("Згенерований пароль:", password)
    print("Пароль скопійовано в буфер обміну.")
