from datetime import datetime

now = datetime.now()
weekdays = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']

print(f"Current time: {now.strftime('%Y-%m-%d %H:%M:%S')}")
print(f"Day: {weekdays[now.weekday()]}")