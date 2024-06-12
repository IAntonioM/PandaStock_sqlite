package com.app.pandastock.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.app.pandastock.R;
import com.app.pandastock.firebase.DetalleVentaDao;
import com.app.pandastock.firebase.FirestoreContract;
import com.app.pandastock.firebase.VentaDao;
import com.app.pandastock.models.DetalleVenta;
import com.app.pandastock.models.Venta;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VentasActivity extends AppCompatActivity {
    Button nuevaVenta;
    VentaDao ventaDao;
    DetalleVentaDao detalleVentaDao;
    LinearLayout llSalesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventas);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        Button btnFiltrar = findViewById(R.id.btnFiltrar);
        btnFiltrar.setOnClickListener(v -> {
            // Lógica para filtrar las ventas
        });

        nuevaVenta = findViewById(R.id.btnNuevaVenta);
        nuevaVenta.setOnClickListener(v -> {
            Intent intent = new Intent(VentasActivity.this, RegistrarVentaActivity.class);
            startActivity(intent);
            finish();
        });

        llSalesList = findViewById(R.id.llSalesList1);

        // Instanciar el DAO de ventas
        ventaDao = new VentaDao();
        detalleVentaDao = new DetalleVentaDao();

        // Cargar las ventas
        cargarVentas();
    }

    private void cargarVentas() {
        ventaDao.getAllVentas(new VentaDao.FirestoreCallback<List<Venta>, Void>() {
            @Override
            public void onComplete(List<Venta> ventas, Void id) {
                llSalesList.removeAllViews();
                if(ventas!=null){

                    for (Venta venta : ventas) {
                        View ventaItem = getLayoutInflater().inflate(R.layout.item_venta, null);

                            TextView tvCliente = ventaItem.findViewById(R.id.tvCliente);
                            TextView tvDetalles = ventaItem.findViewById(R.id.tvDetalles);
                            TextView tvFecha = ventaItem.findViewById(R.id.tvFecha);
                            TextView tvMontoTotal = ventaItem.findViewById(R.id.tvMontoTotal);
                            Button btnEditar = ventaItem.findViewById(R.id.btnEditar);
                            Button btnDetalles = ventaItem.findViewById(R.id.btnDetalles);


                            Date fechaCreacion = venta.getFechaCreacion();
                            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy"); // Formato: 28/05/2022
                            String fechaFormateada = formatoFecha.format(fechaCreacion);
                            tvCliente.setText(venta.getNombreCliente() + " " + venta.getApellidoCliente());
                            tvDetalles.setText("Celular: " + venta.getCelular() + " | DNI: " + venta.getDni());
                            tvFecha.setText("Fecha: " + fechaFormateada);
                            tvMontoTotal.setText("Monto Total: S/ " + venta.getMontoTotal());

                            btnEditar.setOnClickListener(v -> {
                                // Lógica para editar la venta
                            });

                            btnDetalles.setOnClickListener(v -> {
                                mostrarDetallesVenta(venta);
                            });

                            llSalesList.addView(ventaItem);
                    }

                }else{

                }
            }
        });
    }
    private void mostrarDetallesVenta(Venta venta) {
        detalleVentaDao.getDetallesPorVenta(venta.getId(), new DetalleVentaDao.FirestoreCallback<List<DetalleVenta>>() {
            @Override
            public void onComplete(List<DetalleVenta> detallesVenta) {
                if (detallesVenta != null) {
                    // Crear y mostrar el modal con los detalles de la venta
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_detalle_venta, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(VentasActivity.this);
                    builder.setView(dialogView);
                    AlertDialog dialog = builder.create();

                    // Obtener referencias de los TextView del diálogo
                    TextView tvCodigoVenta = dialogView.findViewById(R.id.tvCodigoVenta);
                    TextView tvVendedor = dialogView.findViewById(R.id.tvNombresVendedor);
                    TextView tvMontoTotal = dialogView.findViewById(R.id.tvMontoTotal);

                    // Establecer los valores de los TextView con los datos de la venta
                    tvCodigoVenta.setText("Código Venta: " + venta.getId());

                    // Obtener el nombre del vendedor
                    venta.getEmpleado().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String nombre = documentSnapshot.getString(FirestoreContract.UsuarioEntry.FIELD_NOMBRE);
                                String apellido = documentSnapshot.getString(FirestoreContract.UsuarioEntry.FIELD_APELLIDO);
                                tvVendedor.setText("Vendedor: " + nombre + " " + apellido);
                            }
                        }
                    });

                    // Establecer el monto total de la venta
                    tvMontoTotal.setText("Monto Total: S/ " + venta.getMontoTotal());

                    LinearLayout llDetalleVenta = dialogView.findViewById(R.id.llDetalleVenta);
                    Button btnCerrar = dialogView.findViewById(R.id.btnCerrar);

                    for (DetalleVenta detalle : detallesVenta) {
                        View detalleItem = getLayoutInflater().inflate(R.layout.item_detalle_venta, null);

                        TextView tvProducto = detalleItem.findViewById(R.id.tvProducto);
                        TextView tvCantidad = detalleItem.findViewById(R.id.tvCantidad);
                        TextView tvPrecioUnitario = detalleItem.findViewById(R.id.tvPrecioUnitario);
                        TextView tvSubtotal = detalleItem.findViewById(R.id.tvSubtotal);

                        // Obtener el modelo del producto
                        detalle.getProducto().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    String modelo = documentSnapshot.getString(FirestoreContract.ProductoEntry.FIELD_MODELO);
                                    tvProducto.setText("Producto: " + modelo);
                                }
                            }
                        });

                        // Establecer los valores de los TextView con los datos del detalle de venta
                        tvCantidad.setText("Cantidad: " + detalle.getCantidad());
                        tvPrecioUnitario.setText("Precio Unitario: S/ " + detalle.getPrecioUnitario());
                        tvSubtotal.setText("Subtotal: S/ " + detalle.getSubtotal());

                        llDetalleVenta.addView(detalleItem);
                    }

                    btnCerrar.setOnClickListener(v -> dialog.dismiss());

                    dialog.show();
                } else {
                    Toast.makeText(VentasActivity.this, "No se encontraron detalles para esta venta", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
