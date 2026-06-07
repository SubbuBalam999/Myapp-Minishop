INSERT INTO products (name, description, price, stock_quantity)
SELECT seed.name, seed.description, seed.price, seed.stock_quantity
FROM (
    VALUES
        ('Laptop', 'DevOps practice laptop', 999.99, 10),
        ('Keyboard', 'Mechanical keyboard', 129.99, 25),
        ('Monitor', '27 inch monitor', 299.99, 15)
) AS seed(name, description, price, stock_quantity)
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE products.name = seed.name
);
