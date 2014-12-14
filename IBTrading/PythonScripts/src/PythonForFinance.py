def npv_f(rate, cashflows):
    total = 0.0
    for i, cashflow in enumerate(cashflows):
        total += cashflow / (1 + rate)**i
    return total

def IRR_f(cashflows, interations=100):
    rate = 1.0
    investment = cashflows[0]
    for i in range(1, interations+1):
        rate *= (1 - npv_f(rate, cashflows)/investment)
    return rate