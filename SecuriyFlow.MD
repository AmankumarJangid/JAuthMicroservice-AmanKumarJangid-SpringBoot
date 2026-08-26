## Security Context Flow

```md
[ Request Starts ]
       │
       ▼
1. Filter intercepts the incoming JWT / Cookie.
2. Filter creates a brand-new SecurityContext.
3. Filter loads the user's data into the Current Thread.
       │
       ▼
[ Request Executes ]
       │
       ▼
4. Your Controller runs.
5. Your Database queries execute.
6. The Server prepares the JSON or HTML response.
       │
       ▼
[ Request Ends ]
       │
       ▼
7. Response is sent back to the user's browser.
8. Filter calls SecurityContextHolder.clearContext().
9. The SecurityContext is completely destroyed.
```