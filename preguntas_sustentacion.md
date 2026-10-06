# Preguntas de sustentación — DeepBlue Rescue 

1. **¿Cuál es la responsabilidad del Controller?**
Recibir la petición HTTP, pasársela al Service y devolver la respuesta. No toma decisiones de negocio.
2. **¿Qué diferencia existe entre Controller y Service?**
El Controller maneja HTTP (rutas, JSON, códigos). El Service maneja las reglas del negocio.
3. **¿Por qué Controller no debería usar Repository directamente?**
Porque se saltaría las reglas de negocio y las transacciones, que viven en el Service.
4. **¿Qué hace `@RestController`?**
Marca la clase como controlador REST y convierte lo que retorna a JSON.
5. **¿Qué hace `@RequestMapping`?**
Define la ruta base que atiende el controlador, por ejemplo `/api/animals`.
6. **¿Qué diferencia existe entre `@PathVariable` y `@RequestParam`?**
`@PathVariable` toma un valor de la ruta (`/animals/AN-001`). `@RequestParam` toma un valor después del `?` (`?status=...`).
7. **¿Qué hace `@RequestBody`?**
Convierte el JSON que llega en la petición a un objeto Java.
8. **¿Qué hace `@Valid`?**
Revisa que el objeto recibido cumpla las validaciones (`@NotBlank`, `@NotNull`, etc.) antes de ejecutar el método.
9. **¿Qué diferencia existe entre validación de entrada y regla de negocio?**
La validación revisa que los datos estén bien escritos (campo vacío, null). La regla de negocio revisa que la operación esté permitida (especialista inactivo, transición inválida).
10. **¿Cuándo utilizar GET?**
Para consultar datos, sin modificar nada.
11. **¿Cuándo utilizar POST?**
Para crear un recurso nuevo.
12. **¿Cuándo utilizar PATCH?**
Para cambiar solo una parte de un recurso, como el estado de un caso.
13. **¿Qué significa 200?**
OK: todo salió bien.
14. **¿Qué significa 201?**
Created: se creó un recurso nuevo.
15. **¿Qué significa 400?**
Bad Request: la petición está mal hecha o es inválida.
16. **¿Qué significa 404?**
Not Found: el recurso no existe.
17. **¿Qué significa 409?**
Conflict: la petición es válida, pero rompe una regla de negocio.
18. **¿Qué significa 500?**
Internal Server Error: ocurrió un error inesperado en el servidor.
19. **¿Para qué sirve `ResponseEntity`?**
Para controlar la respuesta: el código de estado y el cuerpo.
20. **¿Qué problema resuelve `@RestControllerAdvice`?**
Maneja los errores de todos los controllers en un solo lugar, sin repetir `try/catch`.
21. **¿Por qué conviene tener un `ErrorResponse` común?**
Para que todos los errores tengan el mismo formato y sean fáciles de entender.
22. **¿Para qué sirve `details`?**
Para dar información extra del error, como qué campo falló y por qué.
23. **¿Qué diferencia existe entre `MethodArgumentNotValidException` y `BusinessRuleException`?**
La primera ocurre cuando falla la validación del request (400). La segunda ocurre cuando se viola una regla de negocio (409).
24. **¿Qué prueba `@WebMvcTest`?**
Solo la capa web: el Controller y el manejo de errores, sin base de datos.
25. **¿Por qué utilizamos un Service mock?**
Para probar solo el Controller, sin depender de cómo funciona el Service.
26. **¿Qué permite probar `MockMvc`?**
Simular peticiones HTTP y revisar el código de estado y el JSON de la respuesta.
27. **¿Por qué el test de Controller no necesita PostgreSQL?**
Porque el Service es falso y no se llega a usar ninguna base de datos.
28. **¿Por qué `verify(..., never())` es útil en validaciones?**
Porque comprueba que, si el request era inválido, el Service nunca se ejecutó.
29. **¿Qué capa debe abrir transacciones?**
El Service.
30. **¿Qué capa debe decidir una transición de estado?**
El Service.

