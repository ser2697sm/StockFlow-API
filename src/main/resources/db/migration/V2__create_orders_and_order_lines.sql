CREATE TABLE orders (
                        order_id BIGSERIAL PRIMARY KEY,
                        status VARCHAR(30) NOT NULL,
                        total NUMERIC(12, 2) NOT NULL,
                        created_at TIMESTAMP NOT NULL,
                        updated_at TIMESTAMP NOT NULL
);

CREATE TABLE order_lines (
                             order_line_id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,
                             quantity INTEGER NOT NULL,
                             unit_price NUMERIC(12, 2) NOT NULL,
                             subtotal NUMERIC(12, 2) NOT NULL,

                             CONSTRAINT fk_order_lines_order
                                 FOREIGN KEY (order_id)
                                     REFERENCES orders(order_id),

                             CONSTRAINT fk_order_lines_product
                                 FOREIGN KEY (product_id)
                                     REFERENCES products(id),

                             CONSTRAINT chk_order_lines_quantity
                                 CHECK (quantity >= 1)
);

CREATE INDEX idx_order_lines_order_id
    ON order_lines(order_id);

CREATE INDEX idx_order_lines_product_id
    ON order_lines(product_id);