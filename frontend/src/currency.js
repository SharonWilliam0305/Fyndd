const rupees = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  currencyDisplay: 'symbol',
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
});

// API amounts are stored in paise. Display rupees with Indian digit grouping.
export const money = (paise) => rupees.format(Number(paise) / 100);
