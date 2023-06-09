# Calculate Liability

Calculate the ECL liability for an entity based on the length of the relevant accounting period (days),
length of AML regulated activity (days) and UK revenue.

**URL**: `/economic-crime-levy-calculator/calculate-liability`

**Method**: `POST`

**Required Request Headers**:

| Header Name   | Header Value   | Description                                |
|---------------|----------------|--------------------------------------------|
| Authorization | Bearer {TOKEN} | A valid bearer token from the auth service |

## Request Body Example

```json
{
  "amlRegulatedActivityLength": 365,
  "relevantApLength": 365,
  "ukRevenue": 10200000
}
```

## Responses

### Calculated liability is returned

**Code**: `200 OK`

**Response Body Example**

```json
{
  "amountDue": {
    "amount": 10000,
    "apportioned": false
  },
  "bands": {
    "small": {
      "from": 0,
      "to": 10200000,
      "amount": 0
    },
    "medium": {
      "from": 10200000,
      "to": 36000000,
      "amount": 10000
    },
    "large": {
      "from": 36000000,
      "to": 1000000000,
      "amount": 36000
    },
    "veryLarge": {
      "from": 1000000000,
      "to": 9223372036854775807,
      "amount": 250000
    },
    "apportioned": false
  },
  "calculatedBand": "Medium"
}
```